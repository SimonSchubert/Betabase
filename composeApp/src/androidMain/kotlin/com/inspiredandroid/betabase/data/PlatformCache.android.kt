package com.inspiredandroid.betabase.data

actual fun platformCacheDirectory(): String? = appContext.cacheDir.absolutePath
