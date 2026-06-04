package com.inspiredandroid.betabase.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun createJsonCache(): JsonCache {
    val root = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        .firstOrNull() as? String
    val dir = root?.let { "$it/json_cache" } ?: return NoopJsonCache
    NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
    return IosJsonCache(dir)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosJsonCache(private val dir: String) : JsonCache {
    override suspend fun read(key: String): ByteArray? = withContext(Dispatchers.Default) {
        val data = NSData.dataWithContentsOfFile("$dir/$key") ?: return@withContext null
        val length = data.length.toInt()
        if (length == 0) return@withContext ByteArray(0)
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, length.convert())
        }
        bytes
    }

    override suspend fun write(key: String, bytes: ByteArray) {
        withContext(Dispatchers.Default) {
            val data = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }
            data.writeToFile("$dir/$key", true)
        }
    }
}

private object NoopJsonCache : JsonCache {
    override suspend fun read(key: String): ByteArray? = null
    override suspend fun write(key: String, bytes: ByteArray) {}
}
