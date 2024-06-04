package com.fatecrl.safehide.services

import android.app.Application

class SafeHideApplication : Application() {

    private val appLifecycleHandler = AppLifecycleHandler()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(appLifecycleHandler)
    }
}