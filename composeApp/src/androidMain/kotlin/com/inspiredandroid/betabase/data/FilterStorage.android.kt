package com.inspiredandroid.betabase.data

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "betabase_filters"
private const val KEY_FILTERS = "filters_json"
private const val KEY_GYMS = "gyms_filters_json"

actual fun createFilterStorage(): FilterStorage = AndroidFilterStorage(
    appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
)

private class AndroidFilterStorage(
    private val prefs: SharedPreferences,
) : FilterStorage {
    override fun load(): CompetitionsFilters? = prefs.getString(KEY_FILTERS, null)?.let(::decodeFilters)

    override fun save(filters: CompetitionsFilters) {
        prefs.edit().putString(KEY_FILTERS, encodeFilters(filters)).apply()
    }

    override fun loadGyms(): GymsFilters? = prefs.getString(KEY_GYMS, null)?.let(::decodeGymsFilters)

    override fun saveGyms(filters: GymsFilters) {
        prefs.edit().putString(KEY_GYMS, encodeGymsFilters(filters)).apply()
    }
}
