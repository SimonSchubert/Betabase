package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable

@Immutable
data class Gym(
    val id: String,
    val name: String,
    val address: String,
    val url: String,
    val imageUrl: String? = null,
    val latitude: Double,
    val longitude: Double,
    val disciplines: Set<Discipline>,
    val boards: Set<Board>,
    val sizeSqm: Int?,
    val openingHours: Map<DayKey, String>,
    val city: String,
)

enum class GymMarkerCategory { BOULDER, LEAD, COMBINED }

fun Gym.markerCategory(): GymMarkerCategory {
    val hasBoulder = Discipline.BOULDER in disciplines
    val hasLead = Discipline.LEAD in disciplines
    return when {
        hasBoulder && hasLead -> GymMarkerCategory.COMBINED
        hasBoulder -> GymMarkerCategory.BOULDER
        hasLead -> GymMarkerCategory.LEAD
        else -> GymMarkerCategory.COMBINED
    }
}

enum class Board(val label: String) {
    MOONBOARD("MoonBoard"),
    KILTER("Kilter"),
    TENSION("Tension"),
    SPRAY("Spray wall"),
    ;

    companion object {
        fun fromRaw(raw: String): Board? = when (raw.lowercase().replace(" ", "").replace("_", "")) {
            "moonboard", "moon" -> MOONBOARD
            "kilter", "kilterboard" -> KILTER
            "tension", "tensionboard" -> TENSION
            "spray", "spraywall" -> SPRAY
            else -> null
        }
    }
}

enum class DayKey(val short: String, val label: String) {
    MON("mon", "Mon"),
    TUE("tue", "Tue"),
    WED("wed", "Wed"),
    THU("thu", "Thu"),
    FRI("fri", "Fri"),
    SAT("sat", "Sat"),
    SUN("sun", "Sun"),
    ;

    companion object {
        fun fromShort(value: String): DayKey? = entries.firstOrNull { it.short == value.lowercase() }
        val ordered: List<DayKey> = entries.toList()
    }
}
