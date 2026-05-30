package com.inspiredandroid.betabase.data

import betabase.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class AthletesRepository(
    private val resourcePath: String = "files/ifsc_athletes.json",
) {
    private val mutex = Mutex()
    private var cached: List<Athlete>? = null

    suspend fun load(): List<Athlete> {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: withContext(Dispatchers.Default) {
                runCatching {
                    val bytes = Res.readBytes(resourcePath)
                    json.decodeFromString<AthletesFile>(bytes.decodeToString())
                        .athletes
                        .map { it.toAthlete() }
                }.getOrDefault(emptyList())
            }.also { cached = it }
        }
    }

    private fun AthleteBundled.toAthlete(): Athlete {
        val countryInfo = normalizeCountry(country)
        return Athlete(
            id = id,
            firstName = first_name,
            lastName = last_name,
            gender = if (gender == "W") AthleteGender.WOMEN else AthleteGender.MEN,
            country = countryInfo?.name ?: country?.takeIf { it.isNotBlank() },
            countryCode = countryInfo?.code,
            photoUrl = photo?.takeIf { it.isNotBlank() },
            wikiUrl = wiki_url,
            youtubeChannelId = youtube_channel_id?.takeIf { it.isNotBlank() },
            birthDate = birth_date?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            lastGold = last_gold?.let { LastGold(year = it.year, venue = it.venue.orEmpty(), competition = it.competition.orEmpty()) },
            lastCompeted = last_competed,
            leadTitles = titles.lead,
            boulderTitles = titles.boulder,
            speedTitles = titles.speed,
            combinedTitles = titles.combined,
            totalTitles = titles.total,
            worldCupSeasonTitles = titles.world_cup_season,
            worldChampionshipTitles = titles.world_championship,
            worldCupBoulder = world_cup_events["boulder"].toMedalCount(),
            worldCupLead = world_cup_events["lead"].toMedalCount(),
            worldCupSpeed = world_cup_events["speed"].toMedalCount(),
            olympic = medals["olympics"].toMedalCount(),
            worldChampionships = medals["world_championships"].toMedalCount(),
            worldGames = medals["world_games"].toMedalCount(),
            europeanChampionships = medals["european_championships"].toMedalCount(),
        )
    }

    private fun MedalCountBundled?.toMedalCount(): MedalCount = if (this == null) MedalCount.Zero else MedalCount(gold = gold, silver = silver, bronze = bronze)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
private data class AthletesFile(
    val athletes: List<AthleteBundled> = emptyList(),
)

@Serializable
private data class AthleteBundled(
    val id: String,
    val first_name: String,
    val last_name: String,
    val gender: String,
    val country: String? = null,
    val photo: String? = null,
    val wiki_url: String,
    val youtube_channel_id: String? = null,
    val titles: TitlesBundled,
    val medals: Map<String, MedalCountBundled> = emptyMap(),
    val world_cup_events: Map<String, MedalCountBundled> = emptyMap(),
    val birth_date: String? = null,
    val last_gold: LastGoldBundled? = null,
    val last_competed: Int? = null,
)

@Serializable
private data class LastGoldBundled(
    val year: Int,
    val venue: String? = null,
    val competition: String? = null,
)

@Serializable
private data class TitlesBundled(
    val lead: Int = 0,
    val boulder: Int = 0,
    val speed: Int = 0,
    val combined: Int = 0,
    val total: Int = 0,
    val world_cup_season: Int = 0,
    val world_championship: Int = 0,
)

@Serializable
private data class MedalCountBundled(
    val gold: Int = 0,
    val silver: Int = 0,
    val bronze: Int = 0,
)
