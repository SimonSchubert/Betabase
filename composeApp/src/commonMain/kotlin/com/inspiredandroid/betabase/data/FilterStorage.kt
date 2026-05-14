package com.inspiredandroid.betabase.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface FilterStorage {
    fun load(): CompetitionsFilters?
    fun save(filters: CompetitionsFilters)
}

expect fun createFilterStorage(): FilterStorage

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class PersistedFilters(
    val sources: List<String> = emptyList(),
    val disciplines: List<String> = emptyList(),
    val rounds: List<String> = emptyList(),
    val genders: List<String> = emptyList(),
)

fun encodeFilters(filters: CompetitionsFilters): String = json.encodeToString(
    PersistedFilters(
        sources = filters.sources.map { it.name },
        disciplines = filters.disciplines.map { it.name },
        rounds = filters.rounds.map { it.name },
        genders = filters.genders.map { it.name },
    ),
)

fun decodeFilters(raw: String): CompetitionsFilters? = runCatching {
    val persisted = json.decodeFromString<PersistedFilters>(raw)
    CompetitionsFilters(
        sources = persisted.sources.mapNotNullTo(mutableSetOf(), ::parseSource),
        disciplines = persisted.disciplines.mapNotNullTo(mutableSetOf(), ::parseDiscipline),
        rounds = persisted.rounds.mapNotNullTo(mutableSetOf(), ::parseRound),
        genders = persisted.genders.mapNotNullTo(mutableSetOf(), ::parseGender),
    )
}.getOrNull()

private fun parseSource(name: String): SourceTag? = runCatching { SourceTag.valueOf(name) }.getOrNull()

private fun parseDiscipline(name: String): Discipline? = runCatching { Discipline.valueOf(name) }.getOrNull()

private fun parseRound(name: String): Round? = runCatching { Round.valueOf(name) }.getOrNull()

private fun parseGender(name: String): Gender? = runCatching { Gender.valueOf(name) }.getOrNull()
