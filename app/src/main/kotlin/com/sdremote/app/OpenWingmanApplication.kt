package com.sdremote.app

import android.app.Application
import timber.log.Timber

class OpenWingmanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
