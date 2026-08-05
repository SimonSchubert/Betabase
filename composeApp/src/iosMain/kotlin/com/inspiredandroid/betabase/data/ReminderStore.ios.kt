package com.inspiredandroid.betabase.data

import platform.Foundation.NSUserDefaults

actual fun createReminderStore(): ReminderStore {
    val defaults = NSUserDefaults.standardUserDefaults
    return ReminderStore(
        get = { key -> defaults.stringForKey(key) },
        put = { key, value -> defaults.setObject(value, key) },
        key = "betabase.reminders_json",
    )
}
