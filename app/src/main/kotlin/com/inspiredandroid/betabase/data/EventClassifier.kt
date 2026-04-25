package com.inspiredandroid.betabase.data

object EventClassifier {

    private val youth = Regex("\\bjeugd\\b|\\byouth\\b|\\bjunior\\b|\\bu1[4-9]\\b|\\bu20\\b")
    private val women = Regex("\\bwomen'?s?\\b|\\bfemale\\b|\\bdames\\b|\\bvrouwen\\b")
    private val men = Regex("\\bmen'?s?\\b|\\bmale\\b|\\bheren\\b|\\bmannen\\b")
    private val semi = Regex("\\bsemi[- ]?final")
    private val final = Regex("\\bfinal")
    private val qual = Regex("\\bquali|\\bqualif")

    fun classify(summary: String): Triple<Gender, Discipline, Round> {
        val lower = summary.lowercase()
        return Triple(detectGender(lower), detectDiscipline(lower), detectRound(lower))
    }

    private fun detectGender(s: String): Gender = when {
        youth.containsMatchIn(s) -> Gender.YOUTH
        women.containsMatchIn(s) -> Gender.WOMEN
        men.containsMatchIn(s) -> Gender.MEN
        else -> Gender.MIXED
    }

    private fun detectDiscipline(s: String): Discipline = when {
        "combined" in s || ("boulder" in s && "lead" in s) -> Discipline.COMBINED
        "boulder" in s -> Discipline.BOULDER
        "lead" in s -> Discipline.LEAD
        "speed" in s -> Discipline.SPEED
        else -> Discipline.OTHER
    }

    private fun detectRound(s: String): Round = when {
        semi.containsMatchIn(s) -> Round.SEMIFINAL
        final.containsMatchIn(s) -> Round.FINAL
        qual.containsMatchIn(s) -> Round.QUALIFICATION
        else -> Round.OTHER
    }
}
