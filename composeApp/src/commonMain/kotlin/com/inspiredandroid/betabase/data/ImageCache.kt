package com.inspiredandroid.betabase.data

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import okio.Path.Companion.toPath

/**
 * Writable cache root for this platform (Android cacheDir, iOS Caches,
 * XDG/desktop cache, null on wasm). Shared by the JSON network cache and the
 * Coil disk cache so path resolution lives in one place.
 */
expect fun platformCacheDirectory(): String?

fun imageCacheDirectory(): String? = platformCacheDirectory()?.let { "$it/coil_image_cache" }

fun setupImageLoader() {
    SingletonImageLoader.setSafe { context -> buildImageLoader(context) }
}

private fun buildImageLoader(context: PlatformContext): ImageLoader {
    val builder = ImageLoader.Builder(context)
    imageCacheDirectory()?.let { dir ->
        builder.diskCache(
            DiskCache.Builder()
                .directory(dir.toPath())
                .maxSizeBytes(64L * 1024 * 1024)
                .build(),
        )
    }
    return builder.build()
}
