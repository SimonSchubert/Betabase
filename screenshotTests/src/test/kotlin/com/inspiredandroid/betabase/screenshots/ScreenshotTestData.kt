package com.inspiredandroid.betabase.screenshots

import com.inspiredandroid.betabase.data.Athlete
import com.inspiredandroid.betabase.data.AthleteFeedItem
import com.inspiredandroid.betabase.data.AthleteGender
import com.inspiredandroid.betabase.data.CompetitionEvent
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.data.IfscVideo
import com.inspiredandroid.betabase.data.MedalCount
import com.inspiredandroid.betabase.data.Round
import com.inspiredandroid.betabase.data.SourceTag
import com.inspiredandroid.betabase.data.YoutubeVideo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

private val zone = TimeZone.of("Europe/Amsterdam")

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
    start = LocalDateTime(2026, month, day, hour, minute),
    timeZone = zone,
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
    event(
        id = "dav-1",
        title = "Berta Block Masters 26",
        series = "Berta Block Masters",
        location = "Berta Block, Berlin",
        month = 6, day = 20, hour = 9,
        source = SourceTag.DAV,
        discipline = Discipline.BOULDER, round = Round.OTHER, gender = Gender.MIXED,
    ),
    event(
        id = "europe-1",
        title = "Europe Series Barcelona",
        series = "World Climbing Europe",
        location = "Barcelona, Spain",
        month = 7, day = 17, allDay = true,
        source = SourceTag.EUROPE,
        discipline = Discipline.BOULDER, round = Round.OTHER, gender = Gender.MIXED,
    ),
    event(
        id = "americas-1",
        title = "World Climbing Pan America Championship Curitiba 2026",
        series = "World Climbing Pan America",
        location = "Curitiba, Brazil",
        month = 8, day = 22, allDay = true,
        source = SourceTag.AMERICAS,
        discipline = Discipline.SPEED, round = Round.OTHER, gender = Gender.MIXED,
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

/**
 * Stream cards that pass [CompetitionsFilters.Default].
 * Video ids match JPEGs under `fixtures/thumbs/` (YouTube mqdefault).
 */
val sampleStreams = listOf(
    IfscVideo(
        id = "zLE-PsXRZCQ",
        title = "Lead finals | Chamonix 2026",
        publishedAt = Instant.parse("2026-04-27T18:00:00Z"),
        scheduledStartTime = null,
        durationSeconds = 2 * 3600 + 38 * 60,
        restrictedRegions = emptyList(),
        discipline = Discipline.LEAD,
        round = Round.FINAL,
        gender = Gender.WOMEN,
    ),
    IfscVideo(
        id = "fas5oeACBBw",
        title = "Lead finals | Innsbruck 2026",
        publishedAt = Instant.parse("2026-04-27T16:00:00Z"),
        scheduledStartTime = null,
        durationSeconds = 2 * 3600 + 44 * 60,
        restrictedRegions = emptyList(),
        discipline = Discipline.LEAD,
        round = Round.FINAL,
        gender = Gender.MEN,
    ),
    IfscVideo(
        id = "REprsz2ymk8",
        title = "Boulder finals | Innsbruck 2026",
        publishedAt = Instant.parse("2026-04-26T19:00:00Z"),
        scheduledStartTime = null,
        durationSeconds = 3 * 3600 + 5 * 60,
        restrictedRegions = emptyList(),
        discipline = Discipline.BOULDER,
        round = Round.FINAL,
        gender = Gender.WOMEN,
    ),
)

private fun sampleAthlete(
    id: String,
    first: String,
    last: String,
    gender: AthleteGender,
    country: String,
    countryCode: String,
    totalTitles: Int,
    /** Live IFSC CDN portrait — downloaded at test time, not committed. */
    photoUrl: String,
    youtubeChannelId: String? = null,
    lastCompeted: Int = 2026,
    wcBoulderGold: Int = 0,
    wcLeadGold: Int = 0,
    wcSpeedGold: Int = 0,
): Athlete = Athlete(
    id = id,
    firstName = first,
    lastName = last,
    gender = gender,
    country = country,
    countryCode = countryCode,
    photoUrl = photoUrl,
    wikiUrl = null,
    ifscProfileUrl = "https://ifsc.results.info/athletes/$id",
    youtubeChannelId = youtubeChannelId,
    xHandle = null,
    birthDate = LocalDate(1999, 1, 15),
    lastGold = null,
    lastCompeted = lastCompeted,
    leadTitles = if (wcLeadGold > 0) 1 else 0,
    boulderTitles = if (wcBoulderGold > 0) 1 else 0,
    speedTitles = if (wcSpeedGold > 0) 1 else 0,
    combinedTitles = 0,
    totalTitles = totalTitles,
    worldCupSeasonTitles = totalTitles.coerceAtMost(4),
    worldChampionshipTitles = (totalTitles / 4).coerceAtLeast(0),
    worldCupBoulder = MedalCount(gold = wcBoulderGold, silver = 0, bronze = 0),
    worldCupLead = MedalCount(gold = wcLeadGold, silver = 0, bronze = 0),
    worldCupSpeed = MedalCount(gold = wcSpeedGold, silver = 0, bronze = 0),
    olympic = MedalCount.Zero,
    worldChampionships = MedalCount.Zero,
    worldGames = MedalCount.Zero,
    europeanChampionships = MedalCount.Zero,
)

// Badge uses Athlete.totalGolds. Ordered like AthletesViewModel.sortedByRank.
// photoUrl values are live IFSC CDN links (downloaded in ScreenshotImageLoader).
val sampleAthletes = listOf(
    sampleAthlete(
        "ifsc-1147", "Janja", "Garnbret", AthleteGender.WOMEN, "Slovenia", "SI", 66,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/7te9giijls89kp0ga6m55c724rpo",
        youtubeChannelId = "UC-janja", wcBoulderGold = 40, wcLeadGold = 26,
    ),
    sampleAthlete(
        "ifsc-2035", "Ja In", "Kim", AthleteGender.WOMEN, "South Korea", "KR", 46,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/qzqj0stmqfwlnbzyh8bik85844hs",
        wcBoulderGold = 28, wcLeadGold = 18,
    ),
    sampleAthlete(
        "ifsc-1214", "Jakob", "Schubert", AthleteGender.MEN, "Austria", "AT", 31,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/9h9a8ojxu0bo0iyl8dve0voxo3dw",
        youtubeChannelId = "UC-jakob", wcLeadGold = 31,
    ),
    sampleAthlete(
        "ifsc-1364", "Adam", "Ondra", AthleteGender.MEN, "Czechia", "CZ", 28,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/zw05rrno2ghv21adtkiy9v3jrstl",
        wcLeadGold = 18, wcBoulderGold = 10,
    ),
    sampleAthlete(
        "ifsc-2848", "Aleksandra", "Miroslaw", AthleteGender.WOMEN, "Poland", "PL", 20,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/j13d09t2pxamsj7xqbzp5gbzckf5",
        wcSpeedGold = 20,
    ),
    sampleAthlete(
        "ifsc-13040", "Sorato", "Anraku", AthleteGender.MEN, "Japan", "JP", 15,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/bk9ifljhby050h6lc4uccx4kzhnf",
        wcBoulderGold = 10, wcLeadGold = 5,
    ),
    sampleAthlete(
        "ifsc-2276", "Tomoa", "Narasaki", AthleteGender.MEN, "Japan", "JP", 13,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/d1eqb73iqdlnbotrh3yz2w1cuydx",
        wcBoulderGold = 13,
    ),
    sampleAthlete(
        "ifsc-1803", "Natalia", "Grossman", AthleteGender.WOMEN, "United States", "US", 12,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/k70ngjczc5vv1fr1a2y300mtaedf",
        wcBoulderGold = 12,
    ),
    sampleAthlete(
        "ifsc-2080", "Chaehyun", "Seo", AthleteGender.WOMEN, "South Korea", "KR", 11,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/uwz0xn5oi19f9ptowv4y8eix85lx",
        wcLeadGold = 11,
    ),
    sampleAthlete(
        "ifsc-3340", "Veddriq", "Leonardo", AthleteGender.MEN, "Indonesia", "ID", 10,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/tf81mknbubdqtxc6pyvssbkpshmu",
        wcSpeedGold = 10,
    ),
    sampleAthlete(
        "ifsc-3471", "Reza", "Alipourshenazandifar", AthleteGender.MEN, "Iran", "IR", 9,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/2qb47nxjoqh3i7s19yuzdn192ic0",
        wcSpeedGold = 9,
    ),
    sampleAthlete(
        "ifsc-1559", "Sean", "Mccoll", AthleteGender.MEN, "Canada", "CA", 8,
        photoUrl = "https://d1n1qj9geboqnb.cloudfront.net/ifsc/public/qty4pdsim6ns9agvu91xwitj7kql",
        wcBoulderGold = 4, wcLeadGold = 4,
    ),
).sortedWith(
    compareByDescending<Athlete> { it.totalGolds }
        .thenByDescending { it.totalTitles }
        .thenBy { it.lastName },
)

val sampleAthleteVideos = listOf(
    AthleteFeedItem(
        athlete = sampleAthletes.first { it.id == "ifsc-1214" },
        video = YoutubeVideo(
            id = "IYjUoe3AVvI",
            title = "B.I.G. - A World First | Full Movie",
            publishedAt = Instant.parse("2026-04-03T10:00:00Z"),
        ),
    ),
    AthleteFeedItem(
        athlete = sampleAthletes.first { it.id == "ifsc-1147" },
        video = YoutubeVideo(
            id = "y6xEaoRcPt0",
            title = "How I Train Before World Cup Climbing",
            publishedAt = Instant.parse("2026-04-10T12:00:00Z"),
        ),
    ),
)
