package com.inspiredandroid.betabase.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object IcsParser {

    data class RawEvent(
        val uid: String?,
        val summary: String?,
        val description: String?,
        val location: String?,
        val url: String?,
        val status: String?,
        val start: LocalDateTime?,
        val startZone: TimeZone?,
        val end: LocalDateTime?,
        val endZone: TimeZone?,
    )

    fun parse(text: String): List<RawEvent> {
        val unfolded = unfold(text)
        val events = mutableListOf<RawEvent>()
        var inEvent = false
        var current = mutableMapOf<String, Pair<Map<String, String>, String>>()

        for (line in unfolded) {
            when {
                line == "BEGIN:VEVENT" -> {
                    inEvent = true
                    current = mutableMapOf()
                }
                line == "END:VEVENT" -> {
                    if (inEvent) events.add(toRawEvent(current))
                    inEvent = false
                }
                inEvent -> {
                    val (name, params, value) = parseProperty(line) ?: continue
                    current[name] = params to value
                }
            }
        }
        return events
    }

    private fun unfold(text: String): List<String> {
        val raw = text.replace("\r\n", "\n").split('\n')
        val out = mutableListOf<String>()
        for (line in raw) {
            if (line.isEmpty()) continue
            if ((line.startsWith(' ') || line.startsWith('\t')) && out.isNotEmpty()) {
                out[out.lastIndex] = out.last() + line.substring(1)
            } else {
                out.add(line)
            }
        }
        return out
    }

    private data class Property(val name: String, val params: Map<String, String>, val value: String)

    private fun parseProperty(line: String): Property? {
        val colon = line.indexOf(':').takeIf { it >= 0 } ?: return null
        val left = line.substring(0, colon)
        val value = line.substring(colon + 1)
        val parts = left.split(';')
        val name = parts[0].uppercase()
        val params = if (parts.size > 1) {
            parts.drop(1).mapNotNull {
                val eq = it.indexOf('=')
                if (eq < 0) null else it.substring(0, eq).uppercase() to it.substring(eq + 1)
            }.toMap()
        } else {
            emptyMap()
        }
        return Property(name, params, value)
    }

    private fun toRawEvent(map: Map<String, Pair<Map<String, String>, String>>): RawEvent {
        fun text(name: String): String? = map[name]?.second?.let(::unescapeText)
        val (start, startZone) = map["DTSTART"]?.let { (params, v) -> parseDateTime(v, params) } ?: (null to null)
        val (end, endZone) = map["DTEND"]?.let { (params, v) -> parseDateTime(v, params) } ?: (null to null)
        return RawEvent(
            uid = text("UID"),
            summary = text("SUMMARY"),
            description = text("DESCRIPTION"),
            location = text("LOCATION"),
            url = map["URL"]?.second,
            status = map["STATUS"]?.second,
            start = start,
            startZone = startZone,
            end = end,
            endZone = endZone,
        )
    }

    private fun parseDateTime(value: String, params: Map<String, String>): Pair<LocalDateTime, TimeZone>? {
        return try {
            when {
                value.endsWith("Z") -> {
                    val core = value.removeSuffix("Z")
                    val ldt = parseCompactDateTime(core) ?: return null
                    val instant = ldt.toInstant(TimeZone.UTC)
                    instant.toLocalDateTime(TimeZone.UTC) to TimeZone.UTC
                }
                params["VALUE"] == "DATE" || value.length == 8 -> {
                    val date = parseCompactDate(value) ?: return null
                    date.atTime(0, 0) to TimeZone.currentSystemDefault()
                }
                else -> {
                    val zone = params["TZID"]?.let { runCatching { TimeZone.of(it) }.getOrNull() }
                        ?: TimeZone.currentSystemDefault()
                    val ldt = parseCompactDateTime(value) ?: return null
                    ldt to zone
                }
            }
        } catch (e: Throwable) {
            null
        }
    }

    private fun parseCompactDate(value: String): LocalDate? {
        if (value.length != 8) return null
        val year = value.substring(0, 4).toIntOrNull() ?: return null
        val month = value.substring(4, 6).toIntOrNull() ?: return null
        val day = value.substring(6, 8).toIntOrNull() ?: return null
        return runCatching { LocalDate(year, month, day) }.getOrNull()
    }

    private fun parseCompactDateTime(value: String): LocalDateTime? {
        if (value.length != 15 || value[8] != 'T') return null
        val date = parseCompactDate(value.substring(0, 8)) ?: return null
        val hour = value.substring(9, 11).toIntOrNull() ?: return null
        val minute = value.substring(11, 13).toIntOrNull() ?: return null
        val second = value.substring(13, 15).toIntOrNull() ?: return null
        return runCatching { date.atTime(LocalTime(hour, minute, second)) }.getOrNull()
    }

    private fun unescapeText(value: String): String =
        buildString(value.length) {
            var i = 0
            while (i < value.length) {
                val c = value[i]
                if (c == '\\' && i + 1 < value.length) {
                    when (val next = value[i + 1]) {
                        'n', 'N' -> append('\n')
                        ',', ';', '\\' -> append(next)
                        else -> {
                            append(c); append(next)
                        }
                    }
                    i += 2
                } else {
                    append(c)
                    i++
                }
            }
        }
}
