package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable

@Immutable
data class GymsFilters(
    val disciplines: Set<Discipline>,
    val requireBoards: Boolean,
) {
    fun toggle(discipline: Discipline) = copy(
        disciplines = if (discipline in disciplines) disciplines - discipline else disciplines + discipline,
    )

    fun toggleBoards() = copy(requireBoards = !requireBoards)

    fun matches(gym: Gym): Boolean {
        val disciplineOk = disciplines.isEmpty() || gym.disciplines.any { it in disciplines }
        val boardsOk = !requireBoards || gym.boards.isNotEmpty()
        return disciplineOk && boardsOk
    }

    companion object {
        val Default = GymsFilters(
            disciplines = setOf(Discipline.BOULDER, Discipline.LEAD),
            requireBoards = false,
        )
    }
}
