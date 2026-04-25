package com.inspiredandroid.betabase.screenshots

import com.inspiredandroid.betabase.data.CompetitionEvent
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.data.Round
import com.inspiredandroid.betabase.data.SourceTag
import java.time.ZoneId
import java.time.ZonedDateTime

private val zone = ZoneId.of("Europe/Amsterdam")

private fun event(
    id: String,
    title: String,
    series: String? = null,
    location: String,
    month: Int,
    day: Int,
    hour: Int = 0,
    minute: Int = 0,
    allDay: Boolean = false,
    source: SourceTag = SourceTag.IFSC,
    discipline: Discipline = Discipline.BOULDER,
    round: Round = Round.FINAL,
    gender: Gender = Gender.WOMEN,
) = CompetitionEvent(
    id = id,
    title = title,
    series = series,
    location = location,
    start = ZonedDateTime.of(2026, month, day, hour, minute, 0, 0, zone),
    end = null,
    url = "https://example.com",
    source = source,
    discipline = discipline,
    round = round,
    gender = gender,
    allDay = allDay,
)

val sampleEvents = listOf(
    event(
        id = "ifsc-1",
        title = "Women's Boulder Final",
        series = "World Cup Innsbruck 2026",
        location = "Innsbruck, Austria",
        month = 5, day = 2, hour = 19, minute = 0,
        discipline = Discipline.BOULDER, round = Round.FINAL, gender = Gender.WOMEN,
    ),
    event(
        id = "ifsc-2",
        title = "Men's Boulder Final",
        series = "World Cup Innsbruck 2026",
        location = "Innsbruck, Austria",
        month = 5, day = 2, hour = 20, minute = 30,
        discipline = Discipline.BOULDER, round = Round.FINAL, gender = Gender.MEN,
    ),
    event(
        id = "sca-1",
        title = "2026 Speed National Cup #1",
        location = "Elevate Climbing Gym, Villawood NSW",
        month = 5, day = 2, hour = 9, minute = 30,
        source = SourceTag.SCA,
        discipline = Discipline.SPEED, round = Round.OTHER, gender = Gender.MIXED,
    ),
    event(
        id = "ifsc-3",
        title = "Women's Lead Final",
        series = "World Cup Chamonix 2026",
        location = "Chamonix, France",
        month = 5, day = 15, hour = 18, minute = 0,
        discipline = Discipline.LEAD, round = Round.FINAL, gender = Gender.WOMEN,
    ),
    event(
        id = "nkbv-1",
        title = "NK LEAD 2026",
        series = "2026 LEAD",
        location = "National Climbing Center, Nieuwegein",
        month = 5, day = 30, allDay = true,
        source = SourceTag.NKBV,
        discipline = Discipline.LEAD, round = Round.OTHER, gender = Gender.MIXED,
    ),
)

val youthSampleEvents = listOf(
    event(
        id = "nkbv-y1",
        title = "Jeugd Boulder Series Limburg 2",
        series = "Jeugd Boulder Series Limburg 2026",
        location = "Boulderkerk, Venlo",
        month = 5, day = 2, allDay = true,
        source = SourceTag.NKBV,
        discipline = Discipline.BOULDER, round = Round.OTHER, gender = Gender.YOUTH,
    ),
    event(
        id = "sca-y1",
        title = "2026 National Youth Championships",
        location = "28 Smith Dr, West Ballina NSW",
        month = 5, day = 14, hour = 7, minute = 0,
        source = SourceTag.SCA,
        discipline = Discipline.OTHER, round = Round.OTHER, gender = Gender.YOUTH,
    ),
    event(
        id = "ifsc-y1",
        title = "Women's Youth A Boulder Final",
        series = "IFSC Youth World Championships",
        location = "Helsinki, Finland",
        month = 8, day = 12, hour = 18, minute = 0,
        discipline = Discipline.BOULDER, round = Round.FINAL, gender = Gender.YOUTH,
    ),
)
