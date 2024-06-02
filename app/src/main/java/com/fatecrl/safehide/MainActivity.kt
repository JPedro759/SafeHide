package com.fatecrl.safehide

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.ActivityMainBinding
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.LockScreenService


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }

        if (!isIgnoringBatteryOptimizations()) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        // Verificar se o usuário está autenticado
        val user = auth.currentUser?.uid

        user.let {
            val lockScreenServiceIntent = Intent(this, LockScreenService::class.java)
            startService(lockScreenServiceIntent)
        }

        binding.apply {
            btnRegister.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, RegisterActivity::class.java)
                )
            }

            loginLink.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, LoginActivity::class.java)
                )
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }
}