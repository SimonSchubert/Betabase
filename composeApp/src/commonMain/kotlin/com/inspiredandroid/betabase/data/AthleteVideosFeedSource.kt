package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/**
 * Fetches the prebuilt athlete-videos JSON — one file holding the latest uploads
 * for every athlete channel, keyed by `channel_id`. It is regenerated server-side
 * by the `refresh-athlete-videos` GitHub Action (see scripts/fetch-athlete-videos.mjs)
 * so the app makes a single request instead of hitting one Atom feed per athlete
 * (which YouTube rate-limits per IP). Same no-secrets, raw-githubusercontent shape
 * as [IfscVideosSource].
 */
class AthleteVideosFeedSource(
    client: HttpClient,
    cache: JsonCache? = null,
    private val feedUrl: String = DEFAULT_URL,
) {
    private val remote = CachedRemoteText(
        client = client,
        cache = cache,
        accept = "application/json, */*",
    )

    suspend fun cached(): Map<String, List<YoutubeVideo>>? = remote.cached(CACHE_KEY)?.let { runCatching { parse(it) }.getOrNull() }

    suspend fun fetch(): Map<String, List<YoutubeVideo>> = parse(remote.fetch(url = feedUrl, key = CACHE_KEY))

    private fun parse(text: String): Map<String, List<YoutubeVideo>> = json.decodeFromString<FeedFile>(text).channels
        .mapValues { (_, videos) -> videos.mapNotNull { it.toDomain() } }

    private fun VideoDto.toDomain(): YoutubeVideo? {
        val published = runCatching { Instant.parse(publishedAt) }.getOrNull() ?: return null
        return YoutubeVideo(id = id, title = title, publishedAt = published)
    }

    @Serializable
    private data class FeedFile(
        val channels: Map<String, List<VideoDto>> = emptyMap(),
    )

    @Serializable
    private data class VideoDto(
        val id: String,
        val title: String = "",
        @SerialName("published_at") val publishedAt: String,
    )

    companion object {
        const val DEFAULT_URL =
            "https://raw.githubusercontent.com/SimonSchubert/Betabase/main/data/athlete_videos.json"
        private const val CACHE_KEY = "athlete_videos.json"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
