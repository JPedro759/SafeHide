package com.fatecrl.safehide.services

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.fatecrl.safehide.LockScreenActivity

class AppLifecycleHandler : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        LockScreenActivity.isEmailCorrect = false
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        if (!LockScreenActivity.isPasswordCorrect) {
            if (activity is LockScreenActivity && !LockScreenActivity.isEmailCorrect) {
                val lockScreenIntent = Intent(activity, LockScreenActivity::class.java)
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(lockScreenIntent)
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}