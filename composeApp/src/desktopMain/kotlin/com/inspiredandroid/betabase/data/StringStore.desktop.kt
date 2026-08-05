package com.inspiredandroid.betabase.data

import java.util.prefs.Preferences

private const val NODE = "com/inspiredandroid/betabase"

actual fun createStringStore(): StringStore {
    val prefs = Preferences.userRoot().node(NODE)
    return object : StringStore {
        override fun get(key: String): String? = prefs.get(key, null)
        override fun put(key: String, value: String) {
            prefs.put(key, value)
        }
    }
}
