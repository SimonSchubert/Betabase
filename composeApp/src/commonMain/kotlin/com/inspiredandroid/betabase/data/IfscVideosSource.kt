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

class IfscVideosSource(
    private val client: HttpClient,
    private val cache: JsonCache? = null,
    private val feedUrl: String = DEFAULT_URL,
) {

    suspend fun cached(): List<IfscVideo>? {
        val bytes = cache?.read(CACHE_KEY) ?: return null
        return runCatching { parse(bytes.decodeToString()) }.getOrNull()
    }

    suspend fun fetch(): List<IfscVideo> {
        val text = downloadText(feedUrl)
        runCatching { cache?.write(CACHE_KEY, text.encodeToByteArray()) }
        return parse(text)
    }

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

    private suspend fun downloadText(url: String): String {
        val response = client.get(url) {
            header(HttpHeaders.Accept, "application/json, */*")
            header(HttpHeaders.UserAgent, "Betabase/0.1")
        }
        if (!response.status.isSuccess()) error("HTTP ${response.status.value} from $url")
        return response.bodyAsText()
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
