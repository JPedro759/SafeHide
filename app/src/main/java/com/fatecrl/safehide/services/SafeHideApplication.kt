package com.fatecrl.safehide.services

import android.app.Application

class SafeHideApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifecycleHandler())
    }
}