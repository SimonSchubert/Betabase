package com.inspiredandroid.betabase.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class CompetitionsRepository(
    private val sources: List<EventSource>,
) {
    fun loadUpcoming(): Flow<LoadProgress> = flow {
        val cutoff = Clock.System.now() - 6.hours

        coroutineScope {
            // One mutable slot per source. Each slot starts seeded with cached
            // events (null for sources without a cache). As each source's
            // network fetch lands, its slot is replaced — so cached IFSC data
            // stays visible until fresh IFSC lands, instead of disappearing the
            // moment a bundled source returns first.
            val slots: MutableList<List<CompetitionEvent>> = sources
                .map { async { it.cached().orEmpty() } }
                .awaitAll()
                .toMutableList()
            if (slots.any { it.isNotEmpty() }) {
                emit(LoadProgress(visibleEvents(slots.flatten(), cutoff), done = false, errors = emptyList()))
            }

            val results = Channel<Pair<Int, Result<List<CompetitionEvent>>>>(capacity = sources.size)
            sources.forEachIndexed { idx, source ->
                launch { results.send(idx to runCatching { source.fetch() }) }
            }
            val errors = mutableListOf<Throwable>()
            for (n in sources.indices) {
                val (idx, result) = results.receive()
                result.onSuccess { slots[idx] = it }
                    .onFailure { errors += it }
                emit(LoadProgress(visibleEvents(slots.flatten(), cutoff), done = n == sources.lastIndex, errors.toList()))
            }
        }
    }

    private fun visibleEvents(events: List<CompetitionEvent>, cutoff: Instant): List<CompetitionEvent> = events.asSequence()
        .filter { it.visibleUntil() >= cutoff }
        .sortedBy { it.start.toInstant(it.timeZone) }
        .toList()

    private fun CompetitionEvent.visibleUntil(): Instant = when {
        end != null -> endOfDay(end.date)
        allDay -> endOfDay(start.date)
        else -> start.toInstant(timeZone)
    }

    private fun CompetitionEvent.endOfDay(date: kotlinx.datetime.LocalDate): Instant = date.plus(1, DateTimeUnit.DAY).atTime(0, 0).toInstant(timeZone)
}

data class LoadProgress(
    val events: List<CompetitionEvent>,
    val done: Boolean,
    val errors: List<Throwable>,
)
