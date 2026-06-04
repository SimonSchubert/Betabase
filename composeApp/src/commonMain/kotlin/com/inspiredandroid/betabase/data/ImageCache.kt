package com.inspiredandroid.betabase.data

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import okio.Path.Companion.toPath

expect fun imageCacheDirectory(): String?

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
