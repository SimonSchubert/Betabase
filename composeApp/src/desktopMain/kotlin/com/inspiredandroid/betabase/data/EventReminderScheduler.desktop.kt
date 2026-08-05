package com.inspiredandroid.betabase.data

actual val remindersSupported: Boolean = false

actual fun createEventReminderScheduler(): EventReminderScheduler = NoOpEventReminderScheduler

private object NoOpEventReminderScheduler : EventReminderScheduler {
    override suspend fun ensurePermission(): Boolean = false
    override fun schedule(record: ReminderRecord) = Unit
    override fun cancel(eventId: String) = Unit
    override fun rescheduleAll(records: Collection<ReminderRecord>) = Unit
}
