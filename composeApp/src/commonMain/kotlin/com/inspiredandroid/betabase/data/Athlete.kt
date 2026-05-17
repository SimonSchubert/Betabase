package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class AthleteGender { WOMEN, MEN }

@Immutable
data class MedalCount(val gold: Int, val silver: Int, val bronze: Int) {
    val total: Int get() = gold + silver + bronze
    val any: Boolean get() = total > 0

    companion object {
        val Zero = MedalCount(0, 0, 0)
    }
}

@Immutable
data class LastGold(
    val year: Int,
    val venue: String,
    val competition: String,
) {
    fun label(): String = buildString {
        append(year)
        if (venue.isNotBlank()) append(" · ").append(venue)
        if (competition.isNotBlank()) append(" · ").append(competition)
    }
}

@Immutable
data class Athlete(
    val id: String,
    val firstName: String,
    val lastName: String,
    val gender: AthleteGender,
    val country: String?,
    val photoUrl: String?,
    val wikiUrl: String,
    val birthDate: LocalDate?,
    val lastGold: LastGold?,
    // Career titles (overall World Cup season + World Championship titles) by discipline.
    val leadTitles: Int,
    val boulderTitles: Int,
    val speedTitles: Int,
    val combinedTitles: Int,
    val totalTitles: Int,
    val worldCupSeasonTitles: Int,
    val worldChampionshipTitles: Int,
    // Per-event World Cup medal totals broken out by discipline. Scraped from
    // the season pages on Wikipedia (2004 + 2006–2025). Authoritative within
    // that window; pre-2004 events aren't on Wikipedia in this form.
    val worldCupBoulder: MedalCount,
    val worldCupLead: MedalCount,
    val worldCupSpeed: MedalCount,
    // Documented podium medals from each athlete's Wikipedia medal-record
    // table — covers Olympics / Worlds / World Games / Euros where the World
    // Cup roll-up wouldn't apply.
    val olympic: MedalCount,
    val worldChampionships: MedalCount,
    val worldGames: MedalCount,
    val europeanChampionships: MedalCount,
) {
    val fullName: String get() = "$firstName $lastName"

    val worldCupTotal: MedalCount = MedalCount(
        gold = worldCupBoulder.gold + worldCupLead.gold + worldCupSpeed.gold,
        silver = worldCupBoulder.silver + worldCupLead.silver + worldCupSpeed.silver,
        bronze = worldCupBoulder.bronze + worldCupLead.bronze + worldCupSpeed.bronze,
    )

    val totalGolds: Int =
        olympic.gold + worldChampionships.gold + worldCupTotal.gold +
            worldGames.gold + europeanChampionships.gold

    fun ageOn(today: LocalDate): Int? {
        val dob = birthDate ?: return null
        return dob.until(today, DateTimeUnit.YEAR).toInt()
    }
}

@OptIn(ExperimentalTime::class)
fun today(): LocalDate {
    val zone = TimeZone.currentSystemDefault()
    val instant = Clock.System.now()
    return instant.toLocalDateTime(zone).date
}
