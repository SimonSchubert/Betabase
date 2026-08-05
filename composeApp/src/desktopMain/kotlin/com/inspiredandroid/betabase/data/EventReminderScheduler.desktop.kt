package com.inspiredandroid.betabase.data

actual val remindersSupported: Boolean = false

actual fun createEventReminderScheduler(): EventReminderScheduler = NoOpEventReminderScheduler
