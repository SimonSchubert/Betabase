package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class IfscVideo(
    val id: String,
    val title: String,
    val publishedAt: Instant,
    val scheduledStartTime: Instant?,
    val durationSeconds: Int,
    val restrictedRegions: List<String>,
    val discipline: Discipline,
    val round: Round,
    val gender: Gender,
) {
    val isUpcoming: Boolean get() = durationSeconds == 0 && scheduledStartTime != null

    val watchUrl: String get() = "https://www.youtube.com/watch?v=$id"

    val thumbnailUrl: String get() = "https://img.youtube.com/vi/$id/mqdefault.jpg"
}
