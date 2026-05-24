package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class YoutubeVideo(
    val id: String,
    val title: String,
    val publishedAt: Instant,
) {
    val watchUrl: String get() = "https://www.youtube.com/watch?v=$id"

    val thumbnailUrl: String get() = "https://img.youtube.com/vi/$id/mqdefault.jpg"
}
