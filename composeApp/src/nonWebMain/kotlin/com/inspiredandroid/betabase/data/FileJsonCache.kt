package com.inspiredandroid.betabase.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

/**
 * Shared disk-backed [JsonCache] for Android, Desktop, and iOS — one
 * implementation instead of three near-identical platform copies.
 */
class FileJsonCache(
    directory: String,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) : JsonCache {
    private val dir: Path = directory.toPath()

    init {
        runCatching { fileSystem.createDirectories(dir) }
    }

    override suspend fun read(key: String): ByteArray? = withContext(Dispatchers.IO) {
        val path = dir / key
        if (!fileSystem.exists(path)) return@withContext null
        runCatching { fileSystem.read(path) { readByteArray() } }.getOrNull()
    }

    override suspend fun write(key: String, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            runCatching {
                val target = dir / key
                val tmp = dir / "$key.tmp"
                fileSystem.write(tmp) { write(bytes) }
                // Atomic replace where the FS supports it; fall back to delete+rename.
                runCatching { fileSystem.atomicMove(tmp, target) }.onFailure {
                    runCatching { fileSystem.delete(target) }
                    fileSystem.atomicMove(tmp, target)
                }
            }
        }
    }
}
