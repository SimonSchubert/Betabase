package com.inspiredandroid.betabase.data

import androidx.compose.runtime.Immutable

@Immutable
data class CompetitionsFilters(
    val sources: Set<SourceTag>,
    val disciplines: Set<Discipline>,
    val rounds: Set<Round>,
    val genders: Set<Gender>,
    val includePara: Boolean = false,
) {
    fun toggle(source: SourceTag) = copy(sources = sources.toggleMember(source))

    fun toggle(discipline: Discipline) = copy(disciplines = disciplines.toggleMember(discipline))

    fun toggle(round: Round) = copy(rounds = rounds.toggleMember(round))

    fun toggle(gender: Gender) = copy(genders = genders.toggleMember(gender))

    fun toggleIncludePara() = copy(includePara = !includePara)

    fun matches(event: CompetitionEvent): Boolean {
        if (event.isPara) {
            return includePara &&
                event.source in sources &&
                (event.discipline == Discipline.OTHER || event.discipline in disciplines) &&
                (event.round == Round.OTHER || event.round in rounds)
        }
        val sourceOk = event.source in sources
        val disciplineOk = event.discipline == Discipline.OTHER || event.discipline in disciplines
        val roundOk = event.round == Round.OTHER || event.round in rounds
        val genderOk = event.gender == Gender.MIXED || event.gender in genders
        return sourceOk && disciplineOk && roundOk && genderOk
    }

    // Streams are IFSC-only, so the source/region filter doesn't apply.
    fun matches(video: IfscVideo): Boolean {
        if (video.isPara) {
            return includePara &&
                (video.discipline == Discipline.OTHER || video.discipline in disciplines) &&
                (video.round == Round.OTHER || video.round in rounds)
        }
        val disciplineOk = video.discipline == Discipline.OTHER || video.discipline in disciplines
        val roundOk = video.round == Round.OTHER || video.round in rounds
        val genderOk = video.gender == Gender.MIXED || video.gender in genders
        return disciplineOk && roundOk && genderOk
    }

    companion object {
        val Default = CompetitionsFilters(
            sources = SourceTag.entries.toSet(),
            disciplines = setOf(Discipline.BOULDER, Discipline.LEAD),
            rounds = setOf(Round.FINAL),
            genders = setOf(Gender.WOMEN, Gender.MEN),
            includePara = false,
        )
    }
}

private fun <T> Set<T>.toggleMember(value: T): Set<T> = if (value in this) this - value else this + value
