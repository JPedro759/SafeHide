package com.fatecrl.safehide

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.ActivityMainBinding
import com.fatecrl.safehide.services.DeviceAdminReceiver
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

        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)

            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "App precisa de permissões de administrador do dispositivo.")

            startActivityForResult(intent, 1)
        } else {
            startLockScreenService()
        }

        auth.currentUser?.let {
            startLockScreenService()
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

    private fun startLockScreenService() {
        val lockScreenServiceIntent = Intent(this, LockScreenService::class.java)
        startService(lockScreenServiceIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d("MainActivity", "Administrador do dispositivo ativado.")
            Toast.makeText(this, "Permissão concedida. O serviço de bloqueio de tela será iniciado.", Toast.LENGTH_SHORT).show()

            startLockScreenService()
        } else {
            Log.d("MainActivity", "Administrador do dispositivo não ativado.")
            Toast.makeText(this, "Permissão negada. O aplicativo pode não funcionar corretamente.", Toast.LENGTH_LONG).show()
        }
    }
}