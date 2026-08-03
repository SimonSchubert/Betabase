package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class IfscVideosSource(
    client: HttpClient,
    cache: JsonCache? = null,
    private val feedUrl: String = DEFAULT_URL,
) {
    private val remote = CachedRemoteText(
        client = client,
        cache = cache,
        accept = "application/json, */*",
    )

    suspend fun cached(): List<IfscVideo>? = remote.cached(CACHE_KEY)?.let { runCatching { parse(it) }.getOrNull() }

    suspend fun fetch(): List<IfscVideo> = parse(remote.fetch(url = feedUrl, key = CACHE_KEY))

    private fun parse(text: String): List<IfscVideo> = json.decodeFromString<List<VideoDto>>(text).mapNotNull { it.toDomain() }

    private fun VideoDto.toDomain(): IfscVideo? {
        val publishedAt = runCatching { Instant.parse(publishedAt) }.getOrNull() ?: return null
        val scheduledStart = scheduledStartTime
            ?.let { runCatching { Instant.parse(it) }.getOrNull() }
        val (gender, discipline, round) = EventClassifier.classify(title)
        return IfscVideo(
            id = videoId,
            title = title,
            publishedAt = publishedAt,
            scheduledStartTime = scheduledStart,
            durationSeconds = duration,
            restrictedRegions = restrictedRegions,
            discipline = discipline,
            round = round,
            gender = gender,
            isPara = EventClassifier.isPara(title),
        )
    }

    @Serializable
    private data class VideoDto(
        @SerialName("video_id") val videoId: String,
        val title: String,
        @SerialName("published_at") val publishedAt: String,
        @SerialName("scheduled_start_time") val scheduledStartTime: String? = null,
        val duration: Int = 0,
        @SerialName("restricted_regions") val restrictedRegions: List<String> = emptyList(),
    )

    companion object {
        const val DEFAULT_URL = "https://raw.githubusercontent.com/sportclimbing/ifsc-videos/main/data/videos.json"
        private const val CACHE_KEY = "ifsc_videos.json"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
