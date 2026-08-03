package com.inspiredandroid.betabase.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads an athlete's latest YouTube uploads on demand. Three layers of cache:
 *  1. Per-process in-memory map — scroll a channel-having card in and out of
 *     view and we never re-hit anything.
 *  2. Disk via [YoutubeChannelSource.cached] — survives cold start.
 *  3. Network refresh via [YoutubeChannelSource.fetch] — always re-fetched
 *     unless the in-memory hit short-circuits the whole call.
 *
 * [load] is a Flow so callers see two emissions (cached, then fresh) on cold
 * start.
 */
class AthleteVideosRepository(
    private val source: YoutubeChannelSource,
    private val maxItems: Int = 6,
) {
    private val mutex = Mutex()
    private val cache = mutableMapOf<String, List<YoutubeVideo>>()

    fun load(channelId: String): Flow<Result<List<YoutubeVideo>>> = flow {
        // In-memory hit short-circuits: no disk or network.
        mutex.withLock { cache[channelId] }?.let {
            emit(Result.success(it))
            return@flow
        }
        loadCachedThenFresh(
            cached = {
                source.cached(channelId)?.let { videos ->
                    process(videos).also { processed ->
                        mutex.withLock { if (channelId !in cache) cache[channelId] = processed }
                    }
                }
            },
            fetch = {
                process(source.fetch(channelId)).also { videos ->
                    mutex.withLock { cache[channelId] = videos }
                }
            },
        ).collect { emit(it) }
    }

    private fun process(videos: List<YoutubeVideo>): List<YoutubeVideo> = videos.sortedByDescending { it.publishedAt }.take(maxItems)
}
