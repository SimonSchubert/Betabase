package com.inspiredandroid.betabase.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface FilterStorage {
    fun load(): CompetitionsFilters?
    fun save(filters: CompetitionsFilters)
    fun loadGyms(): GymsFilters?
    fun saveGyms(filters: GymsFilters)
}

expect fun createFilterStorage(): FilterStorage

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class PersistedFilters(
    val sources: List<String> = emptyList(),
    val disciplines: List<String> = emptyList(),
    val rounds: List<String> = emptyList(),
    val genders: List<String> = emptyList(),
    val includePara: Boolean = false,
)

@Serializable
private data class PersistedGymsFilters(
    val disciplines: List<String> = emptyList(),
    val requireBoards: Boolean = false,
)

fun encodeFilters(filters: CompetitionsFilters): String = json.encodeToString(
    PersistedFilters(
        sources = filters.sources.map { it.name },
        disciplines = filters.disciplines.map { it.name },
        rounds = filters.rounds.map { it.name },
        genders = filters.genders.map { it.name },
        includePara = filters.includePara,
    ),
)

fun decodeFilters(raw: String): CompetitionsFilters? = runCatching {
    val persisted = json.decodeFromString<PersistedFilters>(raw)
    CompetitionsFilters(
        sources = persisted.sources.mapNotNullTo(mutableSetOf(), ::parseSource),
        disciplines = persisted.disciplines.mapNotNullTo(mutableSetOf(), ::parseDiscipline),
        rounds = persisted.rounds.mapNotNullTo(mutableSetOf(), ::parseRound),
        genders = persisted.genders.mapNotNullTo(mutableSetOf(), ::parseGender),
        includePara = persisted.includePara,
    )
}.getOrNull()

fun encodeGymsFilters(filters: GymsFilters): String = json.encodeToString(
    PersistedGymsFilters(
        disciplines = filters.disciplines.map { it.name },
        requireBoards = filters.requireBoards,
    ),
)

fun decodeGymsFilters(raw: String): GymsFilters? = runCatching {
    val persisted = json.decodeFromString<PersistedGymsFilters>(raw)
    GymsFilters(
        disciplines = persisted.disciplines.mapNotNullTo(mutableSetOf(), ::parseDiscipline),
        requireBoards = persisted.requireBoards,
    )
}.getOrNull()

private fun parseSource(name: String): SourceTag? = runCatching { SourceTag.valueOf(name) }.getOrNull()

private fun parseDiscipline(name: String): Discipline? = runCatching { Discipline.valueOf(name) }.getOrNull()

private fun parseRound(name: String): Round? = runCatching { Round.valueOf(name) }.getOrNull()

private fun parseGender(name: String): Gender? = runCatching { Gender.valueOf(name) }.getOrNull()
