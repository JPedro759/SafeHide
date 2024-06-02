package com.fatecrl.safehide.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fatecrl.safehide.LockScreenActivity

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                Log.d("ScreenStateReceiver", "User is present, launching lock screen")

                val lockScreenIntent = Intent(context, LockScreenActivity::class.java)
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(lockScreenIntent)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("ScreenStateReceiver", "Device booted, starting lock screen service")

                val serviceIntent = Intent(context, LockScreenService::class.java)
                context.startService(serviceIntent)
            }
        }
    }
}