package com.inspiredandroid.betabase.data

import java.util.prefs.Preferences

private const val NODE = "com/inspiredandroid/betabase"

actual fun createFilterStorage(): FilterStorage {
    val prefs = Preferences.userRoot().node(NODE)
    return FilterStorage(
        get = { key -> prefs.get(key, null) },
        put = { key, value -> prefs.put(key, value) },
    )
}
