package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

/** Canonical watch / thumbnail URLs for a YouTube video id. Shared by every video model. */
fun youtubeWatchUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

fun youtubeThumbnailUrl(videoId: String): String = "https://img.youtube.com/vi/$videoId/mqdefault.jpg"

@Immutable
data class YoutubeVideo(
    val id: String,
    val title: String,
    val publishedAt: Instant,
) {
    val watchUrl: String get() = youtubeWatchUrl(id)
    val thumbnailUrl: String get() = youtubeThumbnailUrl(id)
}
