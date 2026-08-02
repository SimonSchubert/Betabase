package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient

class IfscEventSource(
    client: HttpClient,
    cache: JsonCache? = null,
    private val feedUrl: String = DEFAULT_URL,
) : EventSource {

    override val tag: SourceTag = SourceTag.IFSC

    private val remote = CachedRemoteText(
        client = client,
        cache = cache,
        accept = "text/calendar, text/plain, */*",
    )

    override suspend fun cached(): List<CompetitionEvent>? = remote.cached(CACHE_KEY)?.let { runCatching { parseEvents(it) }.getOrNull() }

    override suspend fun fetch(): List<CompetitionEvent> = parseEvents(remote.fetch(url = feedUrl, key = CACHE_KEY))

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

    companion object {
        const val DEFAULT_URL = "https://calendar.ifsc.stream"
        private const val CACHE_KEY = "ifsc_events.ics"
    }
}
