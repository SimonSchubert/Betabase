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

/** Shared no-op used by desktop and wasm where local notifications are unsupported. */
object NoOpEventReminderScheduler : EventReminderScheduler {
    override suspend fun ensurePermission(): Boolean = false
    override fun schedule(record: ReminderRecord) = Unit
    override fun cancel(eventId: String) = Unit
    override fun rescheduleAll(records: Collection<ReminderRecord>) = Unit
}

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
