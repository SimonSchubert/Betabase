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

/**
 * Builds a [FilterStorage] over any string key-value store (SharedPreferences,
 * NSUserDefaults, localStorage, java.util.prefs, …). Platforms only supply
 * get/put; encode/decode stays shared.
 */
fun FilterStorage(
    get: (key: String) -> String?,
    put: (key: String, value: String) -> Unit,
    filtersKey: String = "filters_json",
    gymsKey: String = "gyms_filters_json",
): FilterStorage = object : FilterStorage {
    override fun load(): CompetitionsFilters? = get(filtersKey)?.let(::decodeFilters)
    override fun save(filters: CompetitionsFilters) = put(filtersKey, encodeFilters(filters))
    override fun loadGyms(): GymsFilters? = get(gymsKey)?.let(::decodeGymsFilters)
    override fun saveGyms(filters: GymsFilters) = put(gymsKey, encodeGymsFilters(filters))
}

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
    val sources = persisted.sources.mapNotNullTo(mutableSetOf(), ::parseSource)
    if (sources.isEmpty()) {
        sources.addAll(SourceTag.entries)
    }
    val disciplines = persisted.disciplines.mapNotNullTo(mutableSetOf(), ::parseDiscipline)
    val rounds = persisted.rounds.mapNotNullTo(mutableSetOf(), ::parseRound)
    val genders = persisted.genders.mapNotNullTo(mutableSetOf(), ::parseGender)
    CompetitionsFilters(
        sources = sources,
        disciplines = if (disciplines.isEmpty()) CompetitionsFilters.Default.disciplines else disciplines,
        rounds = if (rounds.isEmpty()) CompetitionsFilters.Default.rounds else rounds,
        genders = if (genders.isEmpty()) CompetitionsFilters.Default.genders else genders,
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
