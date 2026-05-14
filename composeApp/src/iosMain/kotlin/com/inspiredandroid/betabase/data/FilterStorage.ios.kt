package com.inspiredandroid.betabase.data

import platform.Foundation.NSUserDefaults

private const val KEY_FILTERS = "betabase.filters_json"

actual fun createFilterStorage(): FilterStorage = IosFilterStorage(NSUserDefaults.standardUserDefaults)

private class IosFilterStorage(
    private val defaults: NSUserDefaults,
) : FilterStorage {
    override fun load(): CompetitionsFilters? = defaults.stringForKey(KEY_FILTERS)?.let(::decodeFilters)

    override fun save(filters: CompetitionsFilters) {
        defaults.setObject(encodeFilters(filters), KEY_FILTERS)
    }
}
