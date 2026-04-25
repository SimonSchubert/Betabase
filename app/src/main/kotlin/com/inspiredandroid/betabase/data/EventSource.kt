package com.inspiredandroid.betabase.data

interface EventSource {
    val tag: SourceTag
    suspend fun fetch(): List<CompetitionEvent>
}
