package com.inspiredandroid.betabase.data

import platform.Foundation.NSUserDefaults

actual fun createStringStore(): StringStore {
    val defaults = NSUserDefaults.standardUserDefaults
    return object : StringStore {
        override fun get(key: String): String? = defaults.stringForKey(key)
        override fun put(key: String, value: String) {
            defaults.setObject(value, key)
        }
    }
}
