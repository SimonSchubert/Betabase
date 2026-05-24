package com.inspiredandroid.betabase.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads an athlete's latest YouTube uploads on demand, caching successful
 * results per channel for the process lifetime so scrolling a channel-having
 * card in and out of view only hits the network once.
 */
class AthleteVideosRepository(
    private val source: YoutubeChannelSource,
    private val maxItems: Int = 6,
) {
    private val mutex = Mutex()
    private val cache = mutableMapOf<String, List<YoutubeVideo>>()

    suspend fun load(channelId: String): Result<List<YoutubeVideo>> {
        mutex.withLock { cache[channelId] }?.let { return Result.success(it) }
        // Fetch outside the lock so different channels load concurrently; callers
        // are expected to de-dupe per channel (see AthletesViewModel.ensureVideos).
        return runCatching {
            source.fetch(channelId)
                .sortedByDescending { it.publishedAt }
                .take(maxItems)
        }.onSuccess { videos -> mutex.withLock { cache[channelId] = videos } }
    }
}
