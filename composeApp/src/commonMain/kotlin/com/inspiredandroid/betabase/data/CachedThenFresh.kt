package com.inspiredandroid.betabase.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Stale-while-revalidate [Flow]: emit a disk/memory snapshot first (if any),
 * then the network result. Cache failures are silent so a stale-schema blob
 * never surfaces as an error; only [fetch] failures become [Result.failure].
 */
fun <T> loadCachedThenFresh(
    cached: suspend () -> T?,
    fetch: suspend () -> T,
): Flow<Result<T>> = flow {
    cached()?.let { emit(Result.success(it)) }
    emit(runCatching { fetch() })
}
