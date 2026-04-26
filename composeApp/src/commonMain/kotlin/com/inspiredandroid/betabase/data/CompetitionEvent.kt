package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

enum class Discipline { BOULDER, LEAD, SPEED, COMBINED, OTHER }
enum class Round { QUALIFICATION, SEMIFINAL, FINAL, OTHER }
enum class Gender { WOMEN, MEN, YOUTH, MIXED }

@Immutable
data class CompetitionEvent(
    val id: String,
    val title: String,
    val series: String?,
    val location: String,
    val start: LocalDateTime,
    val timeZone: TimeZone,
    val end: LocalDateTime?,
    val url: String?,
    val source: SourceTag,
    val discipline: Discipline,
    val round: Round,
    val gender: Gender,
    val allDay: Boolean = false,
)

enum class SourceTag(val displayName: String, val regionLabel: String) {
    IFSC(displayName = "IFSC", regionLabel = "World"),
    NKBV(displayName = "NKBV", regionLabel = "Netherlands"),
    SCA(displayName = "SCA", regionLabel = "Australia"),
}
