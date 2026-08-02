package com.inspiredandroid.betabase.data

actual fun createJsonCache(): JsonCache = FileJsonCache(appContext.cacheDir.resolve("json_cache").absolutePath)
