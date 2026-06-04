package com.inspiredandroid.betabase.data

actual fun createJsonCache(): JsonCache = NoopJsonCache

private object NoopJsonCache : JsonCache {
    override suspend fun read(key: String): ByteArray? = null
    override suspend fun write(key: String, bytes: ByteArray) {}
}
