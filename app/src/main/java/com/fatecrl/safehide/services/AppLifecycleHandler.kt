package com.fatecrl.safehide.services

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.fatecrl.safehide.LockScreenActivity

class AppLifecycleHandler : Application.ActivityLifecycleCallbacks {

    private var numStarted = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (numStarted == 0) {
            // app went to foreground
            Log.d("AppLifecycleHandler", "App in foreground")
        }

        numStarted++
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        numStarted--

        if (numStarted == 0) {
            Log.d("AppLifecycleHandler", "App in background")

            if (activity is LockScreenActivity) {
                Log.d("Password: ", "${activity.isPasswordCorrect()}")
                Log.d("Email: ", "${activity.isEmailCorrect()}")

                if (!activity.isPasswordCorrect() || !activity.isEmailCorrect()) {
                    val lockScreenIntent = Intent(activity, LockScreenActivity::class.java)
                    activity.startActivity(lockScreenIntent)
                }
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}