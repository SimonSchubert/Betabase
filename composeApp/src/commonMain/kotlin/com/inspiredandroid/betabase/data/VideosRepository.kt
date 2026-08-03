package com.inspiredandroid.betabase.data

import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class VideosRepository(
    private val source: IfscVideosSource,
    private val maxItems: Int = 25,
    private val maxAge: Duration = 90.days,
    private val minDurationSeconds: Int = 60,
) {
    /**
     * Emits cached videos first if any are on disk, then the fresh network
     * result. Either emission may carry [Result.failure] only on the network
     * call; cache parse failures are silently dropped so a stale-schema blob
     * doesn't surface as an error.
     */
    fun loadRecent(): Flow<Result<List<IfscVideo>>> {
        val cutoff = Clock.System.now() - maxAge
        return loadCachedThenFresh(
            cached = { source.cached()?.let { process(it, cutoff) } },
            fetch = { process(source.fetch(), cutoff) },
        )
    }

    private fun process(videos: List<IfscVideo>, cutoff: kotlin.time.Instant): List<IfscVideo> = videos.asSequence()
        .filter(::isStreamLike)
        .filter { video ->
            val anchor = video.scheduledStartTime ?: video.publishedAt
            anchor >= cutoff
        }
        .sortedByDescending { it.scheduledStartTime ?: it.publishedAt }
        .take(maxItems)
        .toList()

    // The feed mixes broadcasts, recaps, and YouTube Shorts. Drop the Shorts:
    // social-media clips have duration=0 and no scheduled_start_time, while
    // genuine streams either carry a scheduled time (upcoming/live) or a
    // duration past the threshold (published replay).
    private fun isStreamLike(video: IfscVideo): Boolean {
        if (video.scheduledStartTime != null) return true
        return video.durationSeconds >= minDurationSeconds
    }
}
