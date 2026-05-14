package com.inspiredandroid.betabase.data

import betabase.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class GymsRepository(
    private val indexPath: String = "files/cities/index.json",
    private val cityDir: String = "files/cities",
) {
    private val indexMutex = Mutex()
    private var cachedIndex: List<City>? = null

    suspend fun loadIndex(): List<City> {
        cachedIndex?.let { return it }
        return indexMutex.withLock {
            cachedIndex ?: withContext(Dispatchers.Default) {
                runCatching {
                    val bytes = Res.readBytes(indexPath)
                    json.decodeFromString<CityIndexFile>(bytes.decodeToString())
                        .cities.map { it.toCity() }
                }.getOrDefault(emptyList())
            }.also { cachedIndex = it }
        }
    }

    suspend fun loadCity(id: String): List<Gym> {
        val city = loadIndex().firstOrNull { it.id == id } ?: return emptyList()
        return loadCity(city)
    }

    suspend fun loadAll(): List<Gym> = coroutineScope {
        loadIndex()
            .map { city -> async { loadCity(city) } }
            .awaitAll()
            .flatten()
    }

    private suspend fun loadCity(city: City): List<Gym> = withContext(Dispatchers.Default) {
        runCatching {
            val bytes = Res.readBytes("$cityDir/${city.file}")
            val payload = json.decodeFromString<GymBundledFile>(bytes.decodeToString())
            payload.gyms.map { it.toGym(payload.city.ifEmpty { city.name }) }
        }.getOrDefault(emptyList())
    }

    private fun CityEntry.toCity(): City = City(
        id = id,
        name = name,
        country = country,
        latitude = lat,
        longitude = lng,
        file = file,
    )

    private fun GymBundled.toGym(city: String): Gym = Gym(
        id = id,
        name = name,
        address = address,
        url = url,
        latitude = lat,
        longitude = lng,
        disciplines = disciplines.mapNotNullTo(mutableSetOf()) { parseDiscipline(it) },
        boards = boards.mapNotNullTo(mutableSetOf()) { Board.fromRaw(it) },
        sizeSqm = size_sqm,
        openingHours = opening_hours.mapNotNull { (day, hours) ->
            DayKey.fromShort(day)?.let { it to hours }
        }.toMap(),
        city = city,
    )

    private fun parseDiscipline(raw: String): Discipline? = when (raw.lowercase()) {
        "boulder" -> Discipline.BOULDER
        "lead", "rope" -> Discipline.LEAD
        "speed" -> Discipline.SPEED
        else -> null
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}

data class City(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val file: String,
)

@Serializable
private data class CityIndexFile(
    val version: Int = 1,
    val cities: List<CityEntry> = emptyList(),
)

@Serializable
private data class CityEntry(
    val id: String,
    val name: String,
    val country: String = "",
    val lat: Double,
    val lng: Double,
    val file: String,
)

@Serializable
private data class GymBundledFile(
    val city: String = "",
    val gyms: List<GymBundled> = emptyList(),
)

@Serializable
private data class GymBundled(
    val id: String,
    val name: String,
    val address: String,
    val url: String,
    val lat: Double,
    val lng: Double,
    val disciplines: List<String> = emptyList(),
    val boards: List<String> = emptyList(),
    val size_sqm: Int? = null,
    val opening_hours: Map<String, String> = emptyMap(),
)
