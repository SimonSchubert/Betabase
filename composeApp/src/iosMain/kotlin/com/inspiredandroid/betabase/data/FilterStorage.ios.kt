package com.inspiredandroid.betabase.data

import platform.Foundation.NSUserDefaults

actual fun createFilterStorage(): FilterStorage {
    val defaults = NSUserDefaults.standardUserDefaults
    return FilterStorage(
        get = { key -> defaults.stringForKey(key) },
        put = { key, value -> defaults.setObject(value, key) },
        filtersKey = "betabase.filters_json",
        gymsKey = "betabase.gyms_filters_json",
    )
}
