package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable

@Immutable
data class CompetitionsFilters(
    val sources: Set<SourceTag>,
    val disciplines: Set<Discipline>,
    val rounds: Set<Round>,
    val genders: Set<Gender>,
) {
    fun toggle(source: SourceTag) = copy(sources = sources.toggleMember(source))

    fun toggle(discipline: Discipline) = copy(disciplines = disciplines.toggleMember(discipline))

    fun toggle(round: Round) = copy(rounds = rounds.toggleMember(round))

    fun toggle(gender: Gender) = copy(genders = genders.toggleMember(gender))

    fun matches(event: CompetitionEvent): Boolean {
        val sourceOk = event.source in sources
        val disciplineOk = event.discipline == Discipline.OTHER || event.discipline in disciplines
        val roundOk = event.round == Round.OTHER || event.round in rounds
        val genderOk = event.gender == Gender.MIXED || event.gender in genders
        return sourceOk && disciplineOk && roundOk && genderOk
    }

    companion object {
        val Default = CompetitionsFilters(
            sources = SourceTag.entries.toSet(),
            disciplines = setOf(Discipline.BOULDER, Discipline.LEAD),
            rounds = setOf(Round.FINAL),
            genders = setOf(Gender.WOMEN, Gender.MEN),
        )
    }
}

private fun <T> Set<T>.toggleMember(value: T): Set<T> = if (value in this) this - value else this + value
