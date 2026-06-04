package com.inspiredandroid.betabase

import android.app.Application
import com.inspiredandroid.betabase.data.initBetabase

class BetabaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initBetabase(this)
    }
}
