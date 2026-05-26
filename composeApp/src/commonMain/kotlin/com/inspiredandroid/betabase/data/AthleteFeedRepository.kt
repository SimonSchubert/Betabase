package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Immutable
data class AthleteFeedItem(
    val athlete: Athlete,
    val video: YoutubeVideo,
)

/**
 * Merges the per-athlete YouTube uploads exposed by [AthleteVideosRepository]
 * into one feed sorted by publish date. Channels are fetched in parallel, and
 * the underlying repository caches each channel for the process lifetime so a
 * later call from the athletes screen is a no-op.
 */
class AthleteFeedRepository(
    private val athletesRepository: AthletesRepository,
    private val videosRepository: AthleteVideosRepository,
    private val maxItems: Int = 25,
    private val maxAge: Duration = 90.days,
) {
    private val mutex = Mutex()
    private var cached: List<AthleteFeedItem>? = null

    suspend fun loadRecent(): Result<List<AthleteFeedItem>> {
        mutex.withLock { cached }?.let { return Result.success(it) }
        return runCatching {
            val athletes = athletesRepository.load().filter { !it.youtubeChannelId.isNullOrBlank() }
            if (athletes.isEmpty()) return@runCatching emptyList()
            val cutoff = Clock.System.now() - maxAge
            val merged = coroutineScope {
                athletes.map { athlete ->
                    async {
                        // Per-channel failures shouldn't sink the whole feed — a single
                        // channel that 404s or rate-limits is expected with this many sources.
                        videosRepository.load(athlete.youtubeChannelId!!)
                            .getOrElse { emptyList() }
                            .map { AthleteFeedItem(athlete = athlete, video = it) }
                    }
                }.awaitAll()
            }.flatten()
            merged.asSequence()
                .filter { it.video.publishedAt >= cutoff }
                .sortedByDescending { it.video.publishedAt }
                .take(maxItems)
                .toList()
        }.onSuccess { items -> mutex.withLock { cached = items } }
    }
}
