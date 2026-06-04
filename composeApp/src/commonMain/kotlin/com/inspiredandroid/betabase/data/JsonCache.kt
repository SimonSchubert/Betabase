package com.inspiredandroid.betabase.data

/**
 * Per-platform persistent cache for raw network response bytes (ICS / JSON /
 * atom XML). Used by the network sources to implement stale-while-revalidate
 * — emit cached bytes on cold start, then refresh from network in the
 * background. wasmJs implementation is a no-op (browser HTTP cache covers it).
 */
interface JsonCache {
    suspend fun read(key: String): ByteArray?
    suspend fun write(key: String, bytes: ByteArray)
}

expect fun createJsonCache(): JsonCache
