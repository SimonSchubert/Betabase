package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class IfscEventSource(
    private val client: HttpClient,
    private val feedUrl: String = DEFAULT_URL,
) : EventSource {

    override val tag: SourceTag = SourceTag.IFSC

    override suspend fun fetch(): List<CompetitionEvent> {
        val text = downloadText(feedUrl)
        return IcsParser.parse(text).mapNotNull { it.toEvent() }
    }

    private fun IcsParser.RawEvent.toEvent(): CompetitionEvent? {
        val summary = summary ?: return null
        val start = start ?: return null
        val zone = startZone ?: return null
        val (gender, discipline, round) = EventClassifier.classify(summary)
        return CompetitionEvent(
            id = uid ?: (summary + start.toString()),
            title = summary,
            series = description?.lineSequence()?.firstOrNull()?.takeIf { it.isNotBlank() },
            location = location?.replace("\\,", ",")?.trim().orEmpty(),
            start = start,
            timeZone = zone,
            end = end,
            url = url,
            source = SourceTag.IFSC,
            discipline = discipline,
            round = round,
            gender = gender,
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
    }
}
