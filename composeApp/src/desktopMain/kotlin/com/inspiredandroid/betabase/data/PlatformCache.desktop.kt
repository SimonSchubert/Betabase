package com.inspiredandroid.betabase.data

actual fun platformCacheDirectory(): String? {
    val root = System.getenv("XDG_CACHE_HOME")?.takeIf { it.isNotBlank() }
        ?: "${System.getProperty("user.home")}/.cache"
    return "$root/betabase"
}
