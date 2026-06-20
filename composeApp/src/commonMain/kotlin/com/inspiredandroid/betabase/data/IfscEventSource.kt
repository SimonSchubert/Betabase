package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class IfscEventSource(
    private val client: HttpClient,
    private val cache: JsonCache? = null,
    private val feedUrl: String = DEFAULT_URL,
) : EventSource {

    override val tag: SourceTag = SourceTag.IFSC

    override suspend fun cached(): List<CompetitionEvent>? {
        val bytes = cache?.read(CACHE_KEY) ?: return null
        return runCatching { parseEvents(bytes.decodeToString()) }.getOrNull()
    }

    override suspend fun fetch(): List<CompetitionEvent> {
        val text = downloadText(feedUrl)
        runCatching { cache?.write(CACHE_KEY, text.encodeToByteArray()) }
        return parseEvents(text)
    }

    private fun parseEvents(text: String): List<CompetitionEvent> = IcsParser.parse(text).mapNotNull { it.toEvent() }

    private fun IcsParser.RawEvent.toEvent(): CompetitionEvent? {
        if (status?.equals("CANCELLED", ignoreCase = true) == true) return null
        val summary = summary ?: return null
        val start = start ?: return null
        val zone = startZone ?: return null
        val seriesLine = description?.lineSequence()?.firstOrNull()?.takeIf { it.isNotBlank() }
        val (gender, discipline, round) = EventClassifier.classify(summary)
        return CompetitionEvent(
            id = uid ?: (summary + start.toString()),
            title = summary,
            series = seriesLine,
            location = location?.replace("\\,", ",")?.trim().orEmpty(),
            start = start,
            timeZone = zone,
            end = end,
            url = url,
            source = SourceTag.IFSC,
            discipline = discipline,
            round = round,
            gender = gender,
            isPara = EventClassifier.isPara(listOfNotNull(summary, seriesLine).joinToString(" ")),
        )
    }

    private suspend fun downloadText(url: String): String {
        val response = client.get(url) {
            header(HttpHeaders.Accept, "text/calendar, text/plain, */*")
            header(HttpHeaders.UserAgent, "Betabase/0.1")
        }
        if (!response.status.isSuccess()) error("HTTP ${response.status.value} from $url")
        return response.bodyAsText()
    }

    companion object {
        const val DEFAULT_URL = "https://calendar.ifsc.stream"
        private const val CACHE_KEY = "ifsc_events.ics"
    }
}
