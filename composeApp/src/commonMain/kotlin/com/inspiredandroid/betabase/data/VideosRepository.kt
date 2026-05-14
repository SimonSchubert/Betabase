package com.inspiredandroid.betabase.data

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class VideosRepository(
    private val source: IfscVideosSource,
    private val maxItems: Int = 25,
    private val maxAge: kotlin.time.Duration = 90.days,
    private val minDurationSeconds: Int = 60,
) {
    suspend fun loadRecent(): Result<List<IfscVideo>> = runCatching {
        val cutoff = Clock.System.now() - maxAge
        source.fetch()
            .asSequence()
            .filter(::isStreamLike)
            .filter { video ->
                val anchor = video.scheduledStartTime ?: video.publishedAt
                anchor >= cutoff
            }
            .sortedByDescending { it.scheduledStartTime ?: it.publishedAt }
            .take(maxItems)
            .toList()
    }

    // The feed mixes broadcasts, recaps, and YouTube Shorts. Drop the Shorts:
    // social-media clips have duration=0 and no scheduled_start_time, while
    // genuine streams either carry a scheduled time (upcoming/live) or a
    // duration past the threshold (published replay).
    private fun isStreamLike(video: IfscVideo): Boolean {
        if (video.scheduledStartTime != null) return true
        return video.durationSeconds >= minDurationSeconds
    }
}
