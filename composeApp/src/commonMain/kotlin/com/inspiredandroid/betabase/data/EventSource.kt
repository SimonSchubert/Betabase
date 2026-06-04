package com.inspiredandroid.betabase.data

interface EventSource {
    val tag: SourceTag
    suspend fun fetch(): List<CompetitionEvent>

    /**
     * Cached events from a previous successful [fetch], if any. Default
     * implementation returns null — only sources with a [JsonCache] override
     * this. Used by [CompetitionsRepository] to emit an instant snapshot
     * before the network calls return.
     */
    suspend fun cached(): List<CompetitionEvent>? = null
}
