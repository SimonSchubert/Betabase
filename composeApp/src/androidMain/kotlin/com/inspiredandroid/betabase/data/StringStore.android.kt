package com.inspiredandroid.betabase.data

import android.content.Context

private const val PREFS_NAME = "betabase"
private const val LEGACY_FILTERS_PREFS = "betabase_filters"
private const val LEGACY_REMINDERS_PREFS = "betabase_reminders"
private const val MIGRATED_FLAG = "kv_migrated_v1"

actual fun createStringStore(): StringStore {
    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    migrateLegacyPrefs(prefs)
    return object : StringStore {
        override fun get(key: String): String? = prefs.getString(key, null)
        override fun put(key: String, value: String) {
            prefs.edit().putString(key, value).apply()
        }
    }
}

/** One-shot copy from the pre-unification prefs files into the shared store. */
private fun migrateLegacyPrefs(prefs: android.content.SharedPreferences) {
    if (prefs.getBoolean(MIGRATED_FLAG, false)) return
    val editor = prefs.edit()
    val filters = appContext.getSharedPreferences(LEGACY_FILTERS_PREFS, Context.MODE_PRIVATE)
    filters.getString(PrefKeys.LEGACY_FILTERS, null)?.let { editor.putString(PrefKeys.FILTERS, it) }
    filters.getString(PrefKeys.LEGACY_GYMS_FILTERS, null)?.let { editor.putString(PrefKeys.GYMS_FILTERS, it) }
    val reminders = appContext.getSharedPreferences(LEGACY_REMINDERS_PREFS, Context.MODE_PRIVATE)
    reminders.getString(PrefKeys.LEGACY_REMINDERS, null)?.let { editor.putString(PrefKeys.REMINDERS, it) }
    editor.putBoolean(MIGRATED_FLAG, true).apply()
}
