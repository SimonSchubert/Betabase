package com.inspiredandroid.betabase.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.inspiredandroid.betabase.data.CompetitionEvent
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

val FixedInspectionNow: Instant = Instant.parse("2026-05-01T12:00:00Z")

fun CompetitionEvent.startInstant(): Instant = start.toInstant(timeZone)

fun CompetitionEvent.startIn(zone: TimeZone): LocalDateTime = startInstant().toLocalDateTime(zone)

fun formatRelativeStart(eventStart: Instant, now: Instant): String? {
    val delta = eventStart - now
    return when {
        delta <= (-6).hours -> null
        delta < Duration.ZERO -> "live now"
        delta < 1.minutes -> "starting now"
        delta < 1.hours -> "in ${delta.inWholeMinutes}m"
        delta < 1.days -> {
            val h = delta.inWholeHours
            val m = (delta - h.hours).inWholeMinutes
            if (m == 0L) "in ${h}h" else "in ${h}h ${m}m"
        }
        else -> null
    }
}

@Composable
fun rememberNow(tick: Duration = 30.seconds): State<Instant> = produceState(Clock.System.now()) {
    while (true) {
        value = Clock.System.now()
        delay(tick)
    }
}
