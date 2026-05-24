package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
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
    private val client: HttpClient,
    private val feedUrlBase: String = DEFAULT_FEED_BASE,
) {

    suspend fun fetch(channelId: String): List<YoutubeVideo> {
        val xml = downloadText(feedUrlBase + channelId)
        return parseEntries(xml)
    }

    private suspend fun downloadText(url: String): String {
        val response = client.get(url) {
            header(HttpHeaders.Accept, "application/atom+xml, text/xml, */*")
            header(HttpHeaders.UserAgent, "Betabase/0.1")
        }
        if (!response.status.isSuccess()) error("HTTP ${response.status.value} from $url")
        return response.bodyAsText()
    }

    private fun parseEntries(xml: String): List<YoutubeVideo> =
        ENTRY.findAll(xml).mapNotNull { match ->
            val entry = match.groupValues[1]
            val id = VIDEO_ID.find(entry)?.groupValues?.get(1)
                ?: WATCH_LINK.find(entry)?.groupValues?.get(1)
                ?: return@mapNotNull null
            val rawTitle = TITLE.find(entry)?.groupValues?.get(1)?.trim().orEmpty()
            val published = PUBLISHED.find(entry)?.groupValues?.get(1)
                ?.let { runCatching { Instant.parse(it) }.getOrNull() }
                ?: return@mapNotNull null
            YoutubeVideo(
                id = id,
                title = unescapeXml(rawTitle),
                publishedAt = published,
            )
        }.toList()

    companion object {
        const val DEFAULT_FEED_BASE = "https://www.youtube.com/feeds/videos.xml?channel_id="

        // Each <entry>…</entry> is one video; this also skips the feed-level
        // <title>/<published> that sit outside any entry.
        private val ENTRY = Regex("<entry\\b[^>]*>(.*?)</entry>", RegexOption.DOT_MATCHES_ALL)

        // Namespace prefix is `yt:` in practice; tolerate its absence just in case.
        private val VIDEO_ID = Regex("<(?:yt:)?videoId>([^<]+)</(?:yt:)?videoId>")
        private val WATCH_LINK = Regex("watch\\?v=([\\w-]+)")
        private val TITLE = Regex("<title\\b[^>]*>(.*?)</title>", RegexOption.DOT_MATCHES_ALL)
        private val PUBLISHED = Regex("<published>([^<]+)</published>")

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
