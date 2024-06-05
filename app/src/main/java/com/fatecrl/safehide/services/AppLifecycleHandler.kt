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
        numStarted++
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        numStarted--

        if (numStarted == 0) {
            Log.d("AppLifecycleHandler", "App in background")

            if (activity is LockScreenActivity && !LockScreenActivity.isEmailCorrect) {
                val lockScreenIntent = Intent(activity, LockScreenActivity::class.java)
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(lockScreenIntent)

                LockScreenActivity.isEmailCorrect = true
            }

            LockScreenActivity.isEmailCorrect = false

            if (activity is LockScreenActivity && !LockScreenActivity.isPasswordCorrect) {
                val lockScreenIntent = Intent(activity, LockScreenActivity::class.java)
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(lockScreenIntent)

                LockScreenActivity.isPasswordCorrect = true
                LockScreenActivity.isEmailCorrect = true
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}