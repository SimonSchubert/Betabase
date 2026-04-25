package com.inspiredandroid.betabase.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object IcsParser {

    private val DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val DATE = DateTimeFormatter.ofPattern("yyyyMMdd")

    data class RawEvent(
        val uid: String?,
        val summary: String?,
        val description: String?,
        val location: String?,
        val url: String?,
        val status: String?,
        val start: ZonedDateTime?,
        val end: ZonedDateTime?,
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
        } else emptyMap()
        return Property(name, params, value)
    }

    private fun toRawEvent(map: Map<String, Pair<Map<String, String>, String>>): RawEvent {
        fun text(name: String): String? = map[name]?.second?.let(::unescapeText)
        val start = map["DTSTART"]?.let { (params, v) -> parseDateTime(v, params) }
        val end = map["DTEND"]?.let { (params, v) -> parseDateTime(v, params) }
        return RawEvent(
            uid = text("UID"),
            summary = text("SUMMARY"),
            description = text("DESCRIPTION"),
            location = text("LOCATION"),
            url = map["URL"]?.second,
            status = map["STATUS"]?.second,
            start = start,
            end = end,
        )
    }

    private fun parseDateTime(value: String, params: Map<String, String>): ZonedDateTime? {
        return try {
            when {
                value.endsWith("Z") -> {
                    val ldt = LocalDateTime.parse(value.removeSuffix("Z"), DATE_TIME)
                    ldt.atZone(ZoneOffset.UTC)
                }
                params["VALUE"] == "DATE" || value.length == 8 -> {
                    val date = LocalDate.parse(value, DATE)
                    date.atStartOfDay(ZoneId.systemDefault())
                }
                else -> {
                    val zone = params["TZID"]?.let { runCatching { ZoneId.of(it) }.getOrNull() }
                        ?: ZoneId.systemDefault()
                    LocalDateTime.parse(value, DATE_TIME).atZone(zone)
                }
            }
        } catch (e: Exception) {
            null
        }
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
                        else -> { append(c); append(next) }
                    }
                    i += 2
                } else {
                    append(c)
                    i++
                }
            }
        }
}
