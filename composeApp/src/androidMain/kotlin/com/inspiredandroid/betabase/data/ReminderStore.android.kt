package com.inspiredandroid.betabase.data

import android.content.Context

private const val PREFS_NAME = "betabase_reminders"

actual fun createReminderStore(): ReminderStore {
    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return ReminderStore(
        get = { key -> prefs.getString(key, null) },
        put = { key, value -> prefs.edit().putString(key, value).apply() },
    )
}
