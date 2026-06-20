package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
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
    private val client: HttpClient,
    private val cache: JsonCache? = null,
    private val feedUrl: String = DEFAULT_URL,
) {

    suspend fun cached(): Map<String, List<YoutubeVideo>>? {
        val bytes = cache?.read(CACHE_KEY) ?: return null
        return runCatching { parse(bytes.decodeToString()) }.getOrNull()
    }

    suspend fun fetch(): Map<String, List<YoutubeVideo>> {
        val text = downloadText(feedUrl)
        runCatching { cache?.write(CACHE_KEY, text.encodeToByteArray()) }
        return parse(text)
    }

    private fun parse(text: String): Map<String, List<YoutubeVideo>> = json.decodeFromString<FeedFile>(text).channels
        .mapValues { (_, videos) -> videos.mapNotNull { it.toDomain() } }

    private fun VideoDto.toDomain(): YoutubeVideo? {
        val published = runCatching { Instant.parse(publishedAt) }.getOrNull() ?: return null
        return YoutubeVideo(id = id, title = title, publishedAt = published)
    }

    private suspend fun downloadText(url: String): String {
        val response = client.get(url) {
            header(HttpHeaders.Accept, "application/json, */*")
            header(HttpHeaders.UserAgent, "Betabase/0.1")
        }
        if (!response.status.isSuccess()) error("HTTP ${response.status.value} from $url")
        return response.bodyAsText()
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
