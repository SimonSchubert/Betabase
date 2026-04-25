package com.inspiredandroid.betabase.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.ZonedDateTime

class CompetitionsRepository(
    private val sources: List<EventSource>,
) {
    suspend fun loadUpcoming(): List<CompetitionEvent> = coroutineScope {
        val now = ZonedDateTime.now()
        val results = sources.map { source -> async { source.fetch() } }.awaitAll()
        results
            .flatten()
            .filter { it.start.isAfter(now.minusHours(6)) }
            .sortedBy { it.start }
    }
}
