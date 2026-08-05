package com.inspiredandroid.betabase.data

/**
 * Platform string key-value store (SharedPreferences, NSUserDefaults,
 * java.util.prefs, localStorage, …). Filter and reminder persistence share
 * this so each platform only wires get/put once.
 */
interface StringStore {
    fun get(key: String): String?
    fun put(key: String, value: String)
}

expect fun createStringStore(): StringStore

/** Canonical preference keys used across platforms. */
internal object PrefKeys {
    const val FILTERS = "betabase.filters_json"
    const val GYMS_FILTERS = "betabase.gyms_filters_json"
    const val REMINDERS = "betabase.reminders_json"

    /** Pre-unification short keys (Android prefs + Desktop Preferences). */
    const val LEGACY_FILTERS = "filters_json"
    const val LEGACY_GYMS_FILTERS = "gyms_filters_json"
    const val LEGACY_REMINDERS = "reminders_json"
}
