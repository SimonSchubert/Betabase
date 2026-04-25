package com.inspiredandroid.betabase.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class IfscEventSource(
    private val feedUrl: String = DEFAULT_URL,
) : EventSource {

    override val tag: SourceTag = SourceTag.IFSC

    override suspend fun fetch(): List<CompetitionEvent> = withContext(Dispatchers.IO) {
        val text = downloadText(feedUrl)
        IcsParser.parse(text).mapNotNull { it.toEvent() }
    }

    private fun IcsParser.RawEvent.toEvent(): CompetitionEvent? {
        val summary = summary ?: return null
        val start = start ?: return null
        val (gender, discipline, round) = EventClassifier.classify(summary)
        return CompetitionEvent(
            id = uid ?: (summary + start.toEpochSecond()),
            title = summary,
            series = description?.lineSequence()?.firstOrNull()?.takeIf { it.isNotBlank() },
            location = location?.replace("\\,", ",")?.trim().orEmpty(),
            start = start,
            end = end,
            url = url,
            source = SourceTag.IFSC,
            discipline = discipline,
            round = round,
            gender = gender,
        )
    }

    private fun downloadText(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 20_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "text/calendar, text/plain, */*")
        connection.setRequestProperty("User-Agent", "Betabase/0.1 (Android)")
        try {
            val code = connection.responseCode
            if (code !in 200..299) error("HTTP $code from $url")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val DEFAULT_URL = "https://calendar.ifsc.stream"
    }
}
