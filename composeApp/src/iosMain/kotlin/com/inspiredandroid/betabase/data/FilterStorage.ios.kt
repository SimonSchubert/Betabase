package com.inspiredandroid.betabase.data

import platform.Foundation.NSUserDefaults

private const val KEY_FILTERS = "betabase.filters_json"
private const val KEY_GYMS = "betabase.gyms_filters_json"

actual fun createFilterStorage(): FilterStorage = IosFilterStorage(NSUserDefaults.standardUserDefaults)

private class IosFilterStorage(
    private val defaults: NSUserDefaults,
) : FilterStorage {
    override fun load(): CompetitionsFilters? = defaults.stringForKey(KEY_FILTERS)?.let(::decodeFilters)

    override fun save(filters: CompetitionsFilters) {
        defaults.setObject(encodeFilters(filters), KEY_FILTERS)
    }

    override fun loadGyms(): GymsFilters? = defaults.stringForKey(KEY_GYMS)?.let(::decodeGymsFilters)

    override fun saveGyms(filters: GymsFilters) {
        defaults.setObject(encodeGymsFilters(filters), KEY_GYMS)
    }
}
