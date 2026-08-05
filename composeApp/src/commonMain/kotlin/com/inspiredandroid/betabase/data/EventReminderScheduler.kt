package com.inspiredandroid.betabase.data

/**
 * Platform local-notification scheduling for competition start reminders.
 * Desktop and wasm are no-ops with [remindersSupported] = false.
 */
interface EventReminderScheduler {
    /** Request notification permission if needed. Returns true when notifications may be posted. */
    suspend fun ensurePermission(): Boolean

    fun schedule(record: ReminderRecord)
    fun cancel(eventId: String)
    fun rescheduleAll(records: Collection<ReminderRecord>)
}

/** Whether this platform can show competition start reminders. */
expect val remindersSupported: Boolean

expect fun createEventReminderScheduler(): EventReminderScheduler

fun CompetitionEvent.toReminderRecord(triggerEpochMillis: Long): ReminderRecord = ReminderRecord(
    eventId = id,
    title = title,
    body = when {
        location.isNotBlank() -> location
        !series.isNullOrBlank() -> series
        else -> "Starting now"
    },
    triggerEpochMillis = triggerEpochMillis,
)
