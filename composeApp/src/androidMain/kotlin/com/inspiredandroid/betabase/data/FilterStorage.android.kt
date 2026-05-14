package com.inspiredandroid.betabase.data

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "betabase_filters"
private const val KEY_FILTERS = "filters_json"

private lateinit var appContext: Context

fun initFilterStorage(context: Context) {
    appContext = context.applicationContext
}

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
}
