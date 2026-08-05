package com.inspiredandroid.betabase.data

actual fun createJsonCache(): JsonCache = fileJsonCacheOrNoop(::FileJsonCache)
