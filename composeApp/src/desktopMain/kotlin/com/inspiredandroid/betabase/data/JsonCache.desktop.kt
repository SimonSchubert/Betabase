package com.inspiredandroid.betabase.data

actual fun createJsonCache(): JsonCache {
    val root = System.getenv("XDG_CACHE_HOME")?.takeIf { it.isNotBlank() }
        ?: "${System.getProperty("user.home")}/.cache"
    return FileJsonCache("$root/betabase/json_cache")
}
