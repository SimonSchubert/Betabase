package com.inspiredandroid.betabase.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

private const val DEFAULT_USER_AGENT = "Betabase/0.1"

/**
 * GET a remote text body with the app's standard Accept / User-Agent headers.
 * Shared by every network source so status handling stays in one place.
 */
suspend fun HttpClient.getText(
    url: String,
    accept: String = "*/*",
    userAgent: String = DEFAULT_USER_AGENT,
): String {
    val response = get(url) {
        header(HttpHeaders.Accept, accept)
        header(HttpHeaders.UserAgent, userAgent)
    }
    if (!response.status.isSuccess()) error("HTTP ${response.status.value} from $url")
    return response.bodyAsText()
}

/**
 * Stale-while-revalidate helper for remote text payloads (ICS / JSON / Atom).
 * [cached] returns a previous successful body from [JsonCache] (or null);
 * [fetch] downloads, writes the cache, and returns the fresh body.
 */
class CachedRemoteText(
    private val client: HttpClient,
    private val cache: JsonCache?,
    private val accept: String,
) {
    suspend fun cached(key: String): String? {
        val bytes = cache?.read(key) ?: return null
        return runCatching { bytes.decodeToString() }.getOrNull()
    }

    suspend fun fetch(url: String, key: String): String {
        val text = client.getText(url, accept = accept)
        runCatching { cache?.write(key, text.encodeToByteArray()) }
        return text
    }
}
