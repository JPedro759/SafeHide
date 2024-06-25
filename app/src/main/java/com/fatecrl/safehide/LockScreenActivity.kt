package com.fatecrl.safehide

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.fatecrl.safehide.model.User
import com.fatecrl.safehide.services.FileManager.fileList
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database
import com.fatecrl.safehide.utils.CryptographyUtils.uploadEncryptedFiles
import com.google.firebase.database.DatabaseReference

class LockScreenActivity : Activity() {

    private lateinit var usersRef: DatabaseReference
    private var storedPin: String? = null
    private var storedEmail: String? = null
    private var enteredPin: String? = null

    companion object {
        const val REQUEST_CODE_OVERLAY_PERMISSION = 1234
        var isPasswordCorrect = false
        var isEmailCorrect = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lock_screen)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
        } else {
            setupLockScreen()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                setupLockScreen()
            } else {
                Toast.makeText(
                    this,
                    "Permissão necessária para bloquear a tela!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupLockScreen() {
        usersRef = database.reference.child("users")

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = keyguardManager.newKeyguardLock("LockScreenActivity")
        keyguardLock.disableKeyguard()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val userId = auth.currentUser?.uid

        userId?.let {
            displayPIN(it)
        }

        findViewById<Button>(R.id.btnConfirmPIN).setOnClickListener {
            enteredPin = findViewById<EditText>(R.id.passwordPINInput).text.toString()

            if (storedPin != null && isPasswordCorrect()) {
                isPasswordCorrect = true

                Log.d("fileList", fileList.toString())
                uploadEncryptedFiles(fileList, this)

                finish()
            } else if (storedEmail != null && isEmailCorrect()) {
                isEmailCorrect = true

                finish()
            } else {
                Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayPIN(userId: String) {
        val userRef = usersRef.child(userId)

        userRef.get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    user?.let {
                        findViewById<TextView>(R.id.PIN).text = it.secretPassword
                        storedPin = it.secretPassword
                        storedEmail = it.email
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {}

    private fun isPasswordCorrect(): Boolean {
        return when {
            storedPin != null && enteredPin == storedPin -> true
            else -> false
        }
    }

    private fun isEmailCorrect(): Boolean {
        return when {
            storedPin != null && enteredPin == storedEmail -> true
            else -> false
        }
    }
}