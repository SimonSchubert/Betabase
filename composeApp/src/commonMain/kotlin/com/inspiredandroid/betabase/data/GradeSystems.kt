package com.inspiredandroid.betabase.data

/**
 * Static climbing grade comparison tables.
 *
 * Conversions are approximate community consensus (not exact science).
 * Boulder Font uses capital letters (6A); sport French uses lowercase (6a).
 */
enum class GradeDiscipline { BOULDER, LEAD }

data class GradeSystem(
    val id: String,
    val shortName: String,
    val fullName: String,
    val regionHint: String,
    val description: String,
)

data class GradeRow(
    val labels: Map<String, String>,
)

data class GradeComparisonTable(
    val discipline: GradeDiscipline,
    val systems: List<GradeSystem>,
    val rows: List<GradeRow>,
)

val VScale = GradeSystem(
    id = "v",
    shortName = "V",
    fullName = "Hueco V-scale",
    regionHint = "USA",
    description = "The American boulder grading system, invented at Hueco Tanks. Grades run V0–V17; higher is harder. Dominant in US gyms and outdoor bouldering.",
)

val FontScale = GradeSystem(
    id = "font",
    shortName = "Font",
    fullName = "Fontainebleau",
    regionHint = "Europe",
    description = "The European boulder scale from Fontainebleau, France. Uses grades like 6A, 7C+, 8A. Standard across most of Europe and many international comps.",
)

val JapaneseDankyu = GradeSystem(
    id = "jp",
    shortName = "JP",
    fullName = "Japanese Dankyū",
    regionHint = "Japan",
    description = "The kyū/dan system from Ogawayama, modelled on martial-arts ranks. Starts at 10-kyū (easiest), counts down to 1-kyū, then rises through 1-dan (shodan) up to 6-dan+. Dominant in Japanese gyms and outdoor bouldering.",
)

val FrenchSport = GradeSystem(
    id = "french",
    shortName = "French",
    fullName = "French sport",
    regionHint = "Europe",
    description = "The most common sport/lead grading system worldwide. Grades like 6a, 7c+, 9a — lowercase letters with optional +.",
)

val Yds = GradeSystem(
    id = "yds",
    shortName = "YDS",
    fullName = "Yosemite Decimal System",
    regionHint = "USA",
    description = "US rock climbing scale. Class 5 is free climbing; sport grades use decimals and letters (5.10a–5.15d). Higher decimals are harder.",
)

val Uiaa = GradeSystem(
    id = "uiaa",
    shortName = "UIAA",
    fullName = "UIAA scale",
    regionHint = "Central Europe",
    description = "Roman-numeral scale (IV, VI+, IX−, …) used especially in Germany, Austria, and alpine regions. Still common on older topo guides.",
)

private fun row(vararg pairs: Pair<String, String>) = GradeRow(labels = mapOf(*pairs))

val BoulderGradeTable = GradeComparisonTable(
    discipline = GradeDiscipline.BOULDER,
    systems = listOf(VScale, FontScale, JapaneseDankyu),
    rows = listOf(
        // Japanese kyū/dan spans are approximate (ClimbTokyo / community charts).
        // Each V row picks the closest single rank; some JP grades cover two V grades.
        row("v" to "V0", "font" to "4", "jp" to "7-kyū"),
        row("v" to "V1", "font" to "5", "jp" to "5-kyū"),
        row("v" to "V2", "font" to "5+", "jp" to "4-kyū"),
        row("v" to "V3", "font" to "6A/6A+", "jp" to "3-kyū"),
        row("v" to "V4", "font" to "6B/6B+", "jp" to "2-kyū"),
        row("v" to "V5", "font" to "6C/6C+", "jp" to "1-kyū"),
        row("v" to "V6", "font" to "7A", "jp" to "1-kyū"),
        row("v" to "V7", "font" to "7A+", "jp" to "1-dan"),
        row("v" to "V8", "font" to "7B/7B+", "jp" to "2-dan"),
        row("v" to "V9", "font" to "7C", "jp" to "2-dan"),
        row("v" to "V10", "font" to "7C+", "jp" to "3-dan"),
        row("v" to "V11", "font" to "8A", "jp" to "3-dan"),
        row("v" to "V12", "font" to "8A+", "jp" to "4-dan"),
        row("v" to "V13", "font" to "8B", "jp" to "4-dan"),
        row("v" to "V14", "font" to "8B+", "jp" to "5-dan"),
        row("v" to "V15", "font" to "8C", "jp" to "5-dan"),
        row("v" to "V16", "font" to "8C+", "jp" to "6-dan"),
        row("v" to "V17", "font" to "9A", "jp" to "6-dan"),
    ),
)

val LeadGradeTable = GradeComparisonTable(
    discipline = GradeDiscipline.LEAD,
    systems = listOf(FrenchSport, Yds, Uiaa),
    rows = listOf(
        row("french" to "4a", "yds" to "5.4", "uiaa" to "IV"),
        row("french" to "4b", "yds" to "5.5", "uiaa" to "IV+"),
        row("french" to "4c", "yds" to "5.6", "uiaa" to "V"),
        row("french" to "5a", "yds" to "5.7", "uiaa" to "V+"),
        row("french" to "5b", "yds" to "5.8", "uiaa" to "VI−"),
        row("french" to "5c", "yds" to "5.9", "uiaa" to "VI"),
        row("french" to "6a", "yds" to "5.10a", "uiaa" to "VI+"),
        row("french" to "6a+", "yds" to "5.10b", "uiaa" to "VII−"),
        row("french" to "6b", "yds" to "5.10c", "uiaa" to "VII"),
        row("french" to "6b+", "yds" to "5.10d", "uiaa" to "VII+"),
        row("french" to "6c", "yds" to "5.11a", "uiaa" to "VII+/VIII−"),
        row("french" to "6c+", "yds" to "5.11b", "uiaa" to "VIII−"),
        row("french" to "7a", "yds" to "5.11c/d", "uiaa" to "VIII"),
        row("french" to "7a+", "yds" to "5.12a", "uiaa" to "VIII+"),
        row("french" to "7b", "yds" to "5.12b", "uiaa" to "VIII+/IX−"),
        row("french" to "7b+", "yds" to "5.12c", "uiaa" to "IX−"),
        row("french" to "7c", "yds" to "5.12d", "uiaa" to "IX"),
        row("french" to "7c+", "yds" to "5.13a", "uiaa" to "IX+"),
        row("french" to "8a", "yds" to "5.13b", "uiaa" to "IX+/X−"),
        row("french" to "8a+", "yds" to "5.13c", "uiaa" to "X−"),
        row("french" to "8b", "yds" to "5.13d", "uiaa" to "X"),
        row("french" to "8b+", "yds" to "5.14a", "uiaa" to "X+"),
        row("french" to "8c", "yds" to "5.14b", "uiaa" to "X+/XI−"),
        row("french" to "8c+", "yds" to "5.14c", "uiaa" to "XI−"),
        row("french" to "9a", "yds" to "5.14d", "uiaa" to "XI"),
        row("french" to "9a+", "yds" to "5.15a", "uiaa" to "XI+"),
        row("french" to "9b", "yds" to "5.15b", "uiaa" to "XI+/XII−"),
        row("french" to "9b+", "yds" to "5.15c", "uiaa" to "XII−"),
        row("french" to "9c", "yds" to "5.15d", "uiaa" to "XII"),
    ),
)

fun gradeTableFor(discipline: GradeDiscipline): GradeComparisonTable = when (discipline) {
    GradeDiscipline.BOULDER -> BoulderGradeTable
    GradeDiscipline.LEAD -> LeadGradeTable
}
