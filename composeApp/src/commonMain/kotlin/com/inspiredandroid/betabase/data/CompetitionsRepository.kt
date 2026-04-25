package com.inspiredandroid.betabase.data

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toInstant

class CompetitionsRepository(
    private val sources: List<EventSource>,
) {
    suspend fun loadUpcoming(): List<CompetitionEvent> = coroutineScope {
        val cutoff = Clock.System.now() - 6.hours
        val results = sources.map { source -> async { source.fetch() } }.awaitAll()
        results
            .flatten()
            .filter { event -> event.start.toInstant(event.timeZone) >= cutoff }
            .sortedBy { it.start.toInstant(it.timeZone) }
    }
}
