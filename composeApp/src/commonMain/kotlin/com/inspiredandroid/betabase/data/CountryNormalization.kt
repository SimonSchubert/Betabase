package com.inspiredandroid.betabase.data

/**
 * Canonical country name + ISO-3166 alpha-2 code for an athlete's nationality.
 *
 * The bundled `ifsc_athletes.json` mixes demonyms ("French") with country names
 * ("France") and abbreviations ("U.S."), which would otherwise split the country
 * filter into duplicate buckets. We fold every known spelling onto one canonical
 * entry at parse time so the filter shows one chip per country.
 */
data class CountryInfo(val name: String, val code: String)

private val countriesByRaw: Map<String, CountryInfo> = buildMap {
    fun register(name: String, code: String, vararg aliases: String) {
        val info = CountryInfo(name, code)
        put(name.lowercase(), info)
        aliases.forEach { put(it.lowercase(), info) }
    }
    register("United States", "US", "American", "US", "U.S.", "USA")
    register("Austria", "AT", "Austrian")
    register("Belgium", "BE", "Belgian")
    register("United Kingdom", "GB", "British", "England", "English", "Great Britain")
    register("Canada", "CA", "Canadian")
    register("China", "CN", "Chinese")
    register("Czechia", "CZ", "Czech", "Czech Republic")
    register("France", "FR", "French")
    register("Germany", "DE", "German")
    register("Indonesia", "ID", "Indonesian")
    register("Iran", "IR", "Iranian")
    register("Italy", "IT", "Italian")
    register("Japan", "JP", "Japanese")
    register("Poland", "PL", "Polish")
    register("Russia", "RU", "Russian")
    register("Slovenia", "SI", "Slovenian")
    register("South Korea", "KR", "South Korean", "Korea", "Republic of Korea")
    register("Spain", "ES", "Spanish")
    register("Switzerland", "CH", "Swiss")
    register("Ukraine", "UA", "Ukrainian")
}

/** Folds a raw nationality string onto its canonical country, or null if unknown/blank. */
fun normalizeCountry(raw: String?): CountryInfo? {
    val key = raw?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: return null
    return countriesByRaw[key]
}

/** Unicode regional-indicator flag for an ISO-3166 alpha-2 code, e.g. "FR" → 🇫🇷. */
fun flagEmoji(code: String): String = buildString {
    for (ch in code.uppercase()) {
        if (ch !in 'A'..'Z') continue
        val cp = 0x1F1E6 + (ch - 'A')
        val v = cp - 0x10000
        append((0xD800 + (v shr 10)).toChar())
        append((0xDC00 + (v and 0x3FF)).toChar())
    }
}
