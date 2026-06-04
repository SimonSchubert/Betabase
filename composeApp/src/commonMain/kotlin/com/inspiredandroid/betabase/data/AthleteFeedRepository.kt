package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Immutable
data class AthleteFeedItem(
    val athlete: Athlete,
    val video: YoutubeVideo,
)

/**
 * Merges the per-athlete YouTube uploads exposed by [AthleteVideosRepository]
 * into one feed sorted by publish date. The underlying repository caches each
 * channel both in memory (process lifetime) and on disk (via JsonCache), so
 * cold start can paint an assembled feed from disk before any network call
 * lands. Process-lifetime cache short-circuits subsequent calls entirely.
 */
class AthleteFeedRepository(
    private val athletesRepository: AthletesRepository,
    private val videosRepository: AthleteVideosRepository,
    private val maxItems: Int = 25,
    private val maxAge: Duration = 90.days,
) {
    private val mutex = Mutex()
    private var cached: List<AthleteFeedItem>? = null

    fun loadRecent(): Flow<Result<List<AthleteFeedItem>>> = flow {
        mutex.withLock { cached }?.let {
            emit(Result.success(it))
            return@flow
        }

        val athletes = runCatching {
            athletesRepository.load().filter { !it.youtubeChannelId.isNullOrBlank() }
        }.getOrElse {
            emit(Result.failure(it))
            return@flow
        }
        if (athletes.isEmpty()) {
            emit(Result.success(emptyList()))
            return@flow
        }
        val cutoff = Clock.System.now() - maxAge

        val cachedItems = coroutineScope {
            athletes.map { athlete ->
                async {
                    videosRepository.cachedFor(athlete.youtubeChannelId!!)
                        .orEmpty()
                        .map { AthleteFeedItem(athlete = athlete, video = it) }
                }
            }.awaitAll()
        }.flatten().topRecent(cutoff)
        if (cachedItems.isNotEmpty()) emit(Result.success(cachedItems))

        val freshItems = coroutineScope {
            athletes.map { athlete ->
                async {
                    // Per-channel failures shouldn't sink the whole feed — a single
                    // channel that 404s or rate-limits is expected with this many sources.
                    videosRepository.fetch(athlete.youtubeChannelId!!)
                        .getOrElse { emptyList() }
                        .map { AthleteFeedItem(athlete = athlete, video = it) }
                }
            }.awaitAll()
        }.flatten().topRecent(cutoff)
        mutex.withLock { cached = freshItems }
        emit(Result.success(freshItems))
    }

    private fun List<AthleteFeedItem>.topRecent(cutoff: Instant): List<AthleteFeedItem> =
        asSequence()
            .filter { it.video.publishedAt >= cutoff }
            .sortedByDescending { it.video.publishedAt }
            .take(maxItems)
            .toList()
}
