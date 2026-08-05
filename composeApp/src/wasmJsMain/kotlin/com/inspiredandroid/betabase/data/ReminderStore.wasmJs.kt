package com.inspiredandroid.betabase.data

import kotlinx.browser.localStorage

actual fun createReminderStore(): ReminderStore = ReminderStore(
    get = { key -> localStorage.getItem(key) },
    put = { key, value -> localStorage.setItem(key, value) },
    key = "betabase.reminders_json",
)
