package com.inspiredandroid.betabase.data

import betabase.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class BundledJsonEventSource(
    private val resourcePath: String,
    override val tag: SourceTag,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : EventSource {

    override suspend fun fetch(): List<CompetitionEvent> = withContext(Dispatchers.Default) {
        val bytes = Res.readBytes(resourcePath)
        val text = bytes.decodeToString()
        val payload = json.decodeFromString<BundledFile>(text)
        payload.events.mapNotNull { it.toEvent() }
    }

    private fun BundledEvent.toEvent(): CompetitionEvent? {
        val title = title?.takeIf { it.isNotEmpty() } ?: return null
        val dateStr = date?.takeIf { it.isNotEmpty() } ?: return null
        val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return null
        val timeStr = time?.takeIf { it.isNotEmpty() }
        val (start, allDay) = if (timeStr != null) {
            val parsedTime = runCatching { LocalTime.parse(timeStr) }.getOrNull() ?: return null
            date.atTime(parsedTime) to false
        } else {
            date.atTime(0, 0) to true
        }
        val seriesValue = series?.takeIf { it.isNotEmpty() }
        val (gender, classifiedDiscipline, round) = EventClassifier.classify(
            listOfNotNull(title, seriesValue).joinToString(" "),
        )
        val discipline = parseDiscipline(this.discipline) ?: classifiedDiscipline

        return CompetitionEvent(
            id = id?.takeIf { it.isNotEmpty() } ?: "${tag.name}-$title-$dateStr",
            title = title,
            series = seriesValue,
            location = location.orEmpty(),
            start = start,
            timeZone = zone,
            end = null,
            url = url?.takeIf { it.isNotEmpty() },
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

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
private data class BundledFile(
    val events: List<BundledEvent> = emptyList(),
)

@Serializable
private data class BundledEvent(
    val id: String? = null,
    val title: String? = null,
    val series: String? = null,
    val location: String? = null,
    val date: String? = null,
    val time: String? = null,
    val discipline: String? = null,
    val url: String? = null,
)
