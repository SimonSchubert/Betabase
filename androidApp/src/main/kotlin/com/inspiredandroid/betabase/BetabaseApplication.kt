package com.inspiredandroid.betabase

import android.app.Application
import com.inspiredandroid.betabase.data.initFilterStorage

class BetabaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFilterStorage(this)
    }
}
