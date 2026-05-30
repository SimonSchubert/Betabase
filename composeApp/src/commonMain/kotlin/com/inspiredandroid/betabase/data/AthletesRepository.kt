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
    // Active athletes — small, loaded at startup.
    private val resourcePath: String = "files/ifsc_athletes.json",
    // Long-retired / inactive athletes — larger, loaded lazily only when the user
    // enables "show inactive" (see [loadRetired]).
    private val retiredPath: String = "files/ifsc_athletes_retired.json",
    private val overridesPath: String = "files/ifsc_athletes_overrides.json",
) {
    private val mutex = Mutex()
    private var cached: List<Athlete>? = null
    private val retiredMutex = Mutex()
    private var cachedRetired: List<Athlete>? = null
    private var overridesById: Map<String, AthleteOverride>? = null

    /** The active roster, bundled in the startup file. */
    suspend fun load(): List<Athlete> {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: withContext(Dispatchers.Default) {
                decode(resourcePath)
            }.also { cached = it }
        }
    }

    /**
     * The long-retired roster, loaded on demand the first time the user toggles
     * "show inactive". Kept out of the startup load to keep first paint cheap.
     */
    suspend fun loadRetired(): List<Athlete> {
        cachedRetired?.let { return it }
        return retiredMutex.withLock {
            cachedRetired ?: withContext(Dispatchers.Default) {
                decode(retiredPath)
            }.also { cachedRetired = it }
        }
    }

    private suspend fun decode(path: String): List<Athlete> = runCatching {
        val bytes = Res.readBytes(path)
        val file = json.decodeFromString<AthletesFile>(bytes.decodeToString())
        val byId = loadOverrides()
        file.athletes
            .map { bundled ->
                val o = byId[bundled.id] ?: return@map bundled
                bundled.copy(
                    photo = o.photo?.takeIf { it.isNotBlank() } ?: bundled.photo,
                    last_competed = o.last_competed ?: bundled.last_competed,
                    youtube_channel_id = o.youtube_channel_id?.takeIf { it.isNotBlank() }
                        ?: bundled.youtube_channel_id,
                )
            }
            .map { it.toAthlete() }
    }.getOrDefault(emptyList())

    /** Overrides are shared by both files; parse the file once. */
    private suspend fun loadOverrides(): Map<String, AthleteOverride> {
        overridesById?.let { return it }
        val parsed = runCatching {
            val ob = Res.readBytes(overridesPath)
            json.decodeFromString<AthleteOverridesFile>(ob.decodeToString())
        }.getOrNull()
        return (parsed?.overrides?.associateBy { it.id } ?: emptyMap()).also { overridesById = it }
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
            wikiUrl = wiki_url?.takeIf { it.isNotBlank() },
            ifscProfileUrl = profile_url?.takeIf { it.isNotBlank() }
                ?: ifsc_id?.let { "https://ifsc.results.info/athletes/$it" },
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
    val wiki_url: String? = null,
    val ifsc_id: Int? = null,
    val profile_url: String? = null,
    val youtube_channel_id: String? = null,
    // Omitted from the JSON for athletes with no titles (slim output), so default it.
    val titles: TitlesBundled = TitlesBundled(),
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

// --- Optional overrides layer (human-maintained, survives scraper runs) ---

@Serializable
private data class AthleteOverridesFile(
    val overrides: List<AthleteOverride> = emptyList(),
)

@Serializable
private data class AthleteOverride(
    val id: String,
    val photo: String? = null,
    val last_competed: Int? = null,
    // IFSC can't provide a YouTube channel id (it exposes a handle/URL, not the
    // UC… id the feed needs), so this is the only place to curate one.
    val youtube_channel_id: String? = null,
)
