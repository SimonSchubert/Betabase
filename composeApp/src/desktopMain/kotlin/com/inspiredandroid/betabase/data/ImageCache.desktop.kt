package com.inspiredandroid.betabase.data

actual fun imageCacheDirectory(): String? {
    val root = System.getenv("XDG_CACHE_HOME")?.takeIf { it.isNotBlank() }
        ?: "${System.getProperty("user.home")}/.cache"
    return "$root/betabase/coil_image_cache"
}
