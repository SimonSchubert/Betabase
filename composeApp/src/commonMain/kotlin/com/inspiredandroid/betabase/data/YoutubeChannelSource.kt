package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import kotlin.time.Instant

/**
 * Fetches a YouTube channel's latest uploads from its public Atom feed:
 * https://www.youtube.com/feeds/videos.xml?channel_id=UC…
 *
 * No API key is required, which fits the app's no-secrets architecture (see
 * [IfscVideosSource]). The project ships no XML library, so the Atom payload is
 * parsed by hand in the spirit of IcsParser — extracting only the few fields we
 * need and skipping anything malformed.
 *
 * The feed accepts only the canonical `channel_id` (a 24-char id starting with
 * `UC`), never an `@handle` or `/c/Name` vanity path.
 */
class YoutubeChannelSource(
    client: HttpClient,
    cache: JsonCache? = null,
    private val feedUrlBase: String = DEFAULT_FEED_BASE,
) {
    private val remote = CachedRemoteText(
        client = client,
        cache = cache,
        accept = "application/atom+xml, text/xml, */*",
    )

    suspend fun cached(channelId: String): List<YoutubeVideo>? = remote.cached(cacheKey(channelId))?.let { runCatching { parseEntries(it) }.getOrNull() }

    suspend fun fetch(channelId: String): List<YoutubeVideo> = parseEntries(remote.fetch(url = feedUrlBase + channelId, key = cacheKey(channelId)))

    private fun parseEntries(xml: String): List<YoutubeVideo> {
        // Hand-parse <entry> blocks (no DOT_MATCHES_ALL — not available on all
        // common metadata stdlib variants used by the KMP metadata compiler).
        val results = mutableListOf<YoutubeVideo>()
        var searchFrom = 0
        while (true) {
            val open = xml.indexOf("<entry", searchFrom)
            if (open < 0) break
            val openEnd = xml.indexOf('>', open)
            if (openEnd < 0) break
            val close = xml.indexOf("</entry>", openEnd + 1)
            if (close < 0) break
            val entry = xml.substring(openEnd + 1, close)
            searchFrom = close + "</entry>".length

            // Shorts and regular uploads share the same <entry> shape; the only
            // tell in the Atom feed is the alternate link, which points at
            // `/shorts/<id>` for a Short and `/watch?v=<id>` otherwise. Drop Shorts.
            if (hasShortsLink(entry)) continue

            val id = tagValue(entry, "yt:videoId")
                ?: tagValue(entry, "videoId")
                ?: attrMatch(entry, "watch?v=")
                ?: continue
            val rawTitle = tagValue(entry, "title")?.trim().orEmpty()
            val published = tagValue(entry, "published")
                ?.let { runCatching { Instant.parse(it) }.getOrNull() }
                ?: continue
            results += YoutubeVideo(
                id = id,
                title = unescapeXml(rawTitle),
                publishedAt = published,
            )
        }
        return results
    }

    companion object {
        const val DEFAULT_FEED_BASE = "https://www.youtube.com/feeds/videos.xml?channel_id="

        private fun cacheKey(channelId: String): String = "youtube_channel_$channelId.xml"

        /** Inner text of the first `<name>…</name>` (or namespaced) element. */
        private fun tagValue(xml: String, name: String): String? {
            val open = "<$name"
            val start = xml.indexOf(open)
            if (start < 0) return null
            val afterOpen = xml.indexOf('>', start + open.length)
            if (afterOpen < 0) return null
            // Self-closing / empty.
            if (afterOpen > start && xml[afterOpen - 1] == '/') return null
            val closeTag = "</$name>"
            val end = xml.indexOf(closeTag, afterOpen + 1)
            if (end < 0) return null
            return xml.substring(afterOpen + 1, end)
        }

        /** True when an alternate `<link href="…/shorts/…">` is present. */
        private fun hasShortsLink(entry: String): Boolean {
            var from = 0
            while (true) {
                val linkAt = entry.indexOf("<link", from)
                if (linkAt < 0) return false
                val tagEnd = entry.indexOf('>', linkAt)
                if (tagEnd < 0) return false
                val tag = entry.substring(linkAt, tagEnd)
                if (tag.contains("href=") && tag.contains("/shorts/")) return true
                from = tagEnd + 1
            }
        }

        /** Capture the id after `watch?v=` in an href. */
        private fun attrMatch(xml: String, marker: String): String? {
            val at = xml.indexOf(marker)
            if (at < 0) return null
            val start = at + marker.length
            var end = start
            while (end < xml.length) {
                val c = xml[end]
                if (c.isLetterOrDigit() || c == '_' || c == '-') {
                    end++
                } else {
                    break
                }
            }
            return xml.substring(start, end).takeIf { it.isNotEmpty() }
        }

        // Decode `&amp;` last so an already-decoded `&` isn't mangled.
        private fun unescapeXml(value: String): String = value
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }
}
