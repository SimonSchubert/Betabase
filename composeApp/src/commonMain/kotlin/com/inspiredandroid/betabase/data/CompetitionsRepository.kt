package com.inspiredandroid.betabase.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class CompetitionsRepository(
    private val sources: List<EventSource>,
) {
    fun loadUpcoming(): Flow<LoadProgress> = flow {
        val cutoff = Clock.System.now() - 6.hours
        coroutineScope {
            val results = Channel<Result<List<CompetitionEvent>>>(capacity = sources.size)
            sources.forEach { source ->
                launch { results.send(runCatching { source.fetch() }) }
            }
            val accumulated = mutableListOf<CompetitionEvent>()
            val errors = mutableListOf<Throwable>()
            for (index in sources.indices) {
                val result = results.receive()
                result.onSuccess { accumulated += it }
                    .onFailure { errors += it }
                val visible = accumulated
                    .asSequence()
                    .filter { it.start.toInstant(it.timeZone) >= cutoff }
                    .sortedBy { it.start.toInstant(it.timeZone) }
                    .toList()
                emit(LoadProgress(visible, done = index == sources.lastIndex, errors.toList()))
            }
        }
    }
}

data class LoadProgress(
    val events: List<CompetitionEvent>,
    val done: Boolean,
    val errors: List<Throwable>,
)
