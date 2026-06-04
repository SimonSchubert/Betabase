package com.inspiredandroid.betabase.data

import android.content.Context

internal lateinit var appContext: Context
    private set

fun initBetabase(context: Context) {
    appContext = context.applicationContext
}
