package com.inspiredandroid.betabase.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual fun createJsonCache(): JsonCache = AndroidJsonCache(
    appContext.cacheDir.resolve("json_cache").apply { mkdirs() },
)

private class AndroidJsonCache(private val dir: File) : JsonCache {
    override suspend fun read(key: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = dir.resolve(key)
        if (file.exists()) runCatching { file.readBytes() }.getOrNull() else null
    }

    override suspend fun write(key: String, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            runCatching {
                val tmp = dir.resolve("$key.tmp")
                tmp.writeBytes(bytes)
                tmp.renameTo(dir.resolve(key))
            }
        }
    }
}
