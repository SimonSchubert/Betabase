package com.inspiredandroid.betabase.data

actual fun imageCacheDirectory(): String? = appContext.cacheDir.resolve("coil_image_cache").absolutePath
