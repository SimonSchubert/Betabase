package com.inspiredandroid.betabase.data

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun createJsonCache(): JsonCache {
    val root = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        .firstOrNull() as? String
        ?: return NoopJsonCache
    return FileJsonCache("$root/json_cache")
}
