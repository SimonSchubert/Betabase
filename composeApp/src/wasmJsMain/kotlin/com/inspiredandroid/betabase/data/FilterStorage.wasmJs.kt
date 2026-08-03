package com.inspiredandroid.betabase.data

import kotlinx.browser.localStorage

actual fun createFilterStorage(): FilterStorage = FilterStorage(
    get = { key -> localStorage.getItem(key) },
    put = { key, value -> localStorage.setItem(key, value) },
    filtersKey = "betabase.filters_json",
    gymsKey = "betabase.gyms_filters_json",
)
