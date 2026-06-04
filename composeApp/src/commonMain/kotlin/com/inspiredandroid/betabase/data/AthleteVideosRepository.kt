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
 * start. [cachedFor] / [fetch] are single-shot helpers used by
 * [AthleteFeedRepository] when fanning out across many channels.
 */
class AthleteVideosRepository(
    private val source: YoutubeChannelSource,
    private val maxItems: Int = 6,
) {
    private val mutex = Mutex()
    private val cache = mutableMapOf<String, List<YoutubeVideo>>()

    fun load(channelId: String): Flow<Result<List<YoutubeVideo>>> = flow {
        mutex.withLock { cache[channelId] }?.let {
            emit(Result.success(it))
            return@flow
        }
        source.cached(channelId)?.let { videos ->
            val processed = process(videos)
            mutex.withLock { if (channelId !in cache) cache[channelId] = processed }
            emit(Result.success(processed))
        }
        val networkResult = runCatching { process(source.fetch(channelId)) }
        networkResult.onSuccess { videos -> mutex.withLock { cache[channelId] = videos } }
        emit(networkResult)
    }

    /** Disk-cache snapshot only; no network. Used by the athlete-feed assembler. */
    suspend fun cachedFor(channelId: String): List<YoutubeVideo>? {
        mutex.withLock { cache[channelId] }?.let { return it }
        val videos = source.cached(channelId) ?: return null
        val processed = process(videos)
        mutex.withLock { if (channelId !in cache) cache[channelId] = processed }
        return processed
    }

    /** Network fetch, populates the in-memory cache on success. */
    suspend fun fetch(channelId: String): Result<List<YoutubeVideo>> = runCatching {
        process(source.fetch(channelId))
    }.onSuccess { videos -> mutex.withLock { cache[channelId] = videos } }

    private fun process(videos: List<YoutubeVideo>): List<YoutubeVideo> =
        videos.sortedByDescending { it.publishedAt }.take(maxItems)
}
