package com.inspiredandroid.betabase.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class BundledJsonEventSource(
    private val context: Context,
    private val assetName: String,
    override val tag: SourceTag,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : EventSource {

    override suspend fun fetch(): List<CompetitionEvent> = withContext(Dispatchers.IO) {
        val text = context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)
        val arr = root.getJSONArray("events")
        val out = ArrayList<CompetitionEvent>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val event = obj.toEvent() ?: continue
            out += event
        }
        out
    }

    private fun JSONObject.toEvent(): CompetitionEvent? {
        val title = optStringOrNull("title") ?: return null
        val dateStr = optStringOrNull("date") ?: return null
        val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return null
        val timeStr = optStringOrNull("time")
        val (start, allDay) = if (timeStr != null) {
            val time = runCatching { LocalTime.parse(timeStr) }.getOrNull()
                ?: return null
            ZonedDateTime.of(date, time, zone) to false
        } else {
            date.atStartOfDay(zone) to true
        }
        val series = optStringOrNull("series")
        val (gender, classifiedDiscipline, round) = EventClassifier.classify(
            listOfNotNull(title, series).joinToString(" "),
        )
        val discipline = parseDiscipline(optStringOrNull("discipline")) ?: classifiedDiscipline

        return CompetitionEvent(
            id = optStringOrNull("id") ?: "${tag.name}-$title-$dateStr",
            title = title,
            series = series,
            location = optStringOrNull("location").orEmpty(),
            start = start,
            end = null,
            url = optStringOrNull("url"),
            source = tag,
            discipline = discipline,
            round = round,
            gender = gender,
            allDay = allDay,
        )
    }

    private fun parseDiscipline(raw: String?): Discipline? = when (raw?.lowercase()) {
        "boulder" -> Discipline.BOULDER
        "lead" -> Discipline.LEAD
        "speed" -> Discipline.SPEED
        "combined", "boulder & lead" -> Discipline.COMBINED
        else -> null
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key).takeIf { it.isNotEmpty() } else null
}
