package com.inspiredandroid.betabase.data

import java.util.prefs.Preferences

private const val NODE = "com/inspiredandroid/betabase"
private const val KEY_FILTERS = "filters_json"
private const val KEY_GYMS = "gyms_filters_json"

actual fun createFilterStorage(): FilterStorage = DesktopFilterStorage(Preferences.userRoot().node(NODE))

private class DesktopFilterStorage(
    private val prefs: Preferences,
) : FilterStorage {
    override fun load(): CompetitionsFilters? = prefs.get(KEY_FILTERS, null)?.let(::decodeFilters)

    override fun save(filters: CompetitionsFilters) {
        prefs.put(KEY_FILTERS, encodeFilters(filters))
    }

    override fun loadGyms(): GymsFilters? = prefs.get(KEY_GYMS, null)?.let(::decodeGymsFilters)

    override fun saveGyms(filters: GymsFilters) {
        prefs.put(KEY_GYMS, encodeGymsFilters(filters))
    }
}
