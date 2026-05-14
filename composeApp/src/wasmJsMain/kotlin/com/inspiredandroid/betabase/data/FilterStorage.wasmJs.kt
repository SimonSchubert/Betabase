package com.inspiredandroid.betabase.data

import kotlinx.browser.localStorage

private const val KEY_FILTERS = "betabase.filters_json"

actual fun createFilterStorage(): FilterStorage = WasmJsFilterStorage()

private class WasmJsFilterStorage : FilterStorage {
    override fun load(): CompetitionsFilters? = localStorage.getItem(KEY_FILTERS)?.let(::decodeFilters)

    override fun save(filters: CompetitionsFilters) {
        localStorage.setItem(KEY_FILTERS, encodeFilters(filters))
    }
}
