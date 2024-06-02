package com.fatecrl.safehide

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.fatecrl.safehide.model.User
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database
import com.google.firebase.database.DatabaseReference

class LockScreenActivity : Activity() {

    private lateinit var usersRef: DatabaseReference

    private var storedPin: String? = null
    private var storedEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lock_screen)

        usersRef = database.reference.child("users")

        // Tornar a atividade em tela cheia
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Desabilitar o bloqueio padrão de tela
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = keyguardManager.newKeyguardLock("LockScreenActivity")
        keyguardLock.disableKeyguard()

        // Obtém o ID do usuário atualmente autenticado
        val userId = auth.currentUser?.uid

        userId?.let {
            displayPIN(it)
        }

        findViewById<Button>(R.id.btnConfirmPIN).setOnClickListener {
            val enteredPin = findViewById<EditText>(R.id.passwordPINInput).text.toString()

            if (storedPin != null && enteredPin == storedPin) {
                finish()

                // Aqui a função de criptografia e a função de upload serão chamadas!
            } else if (storedEmail != null && enteredPin == storedEmail) {
                finish()
            }
            else {
                Toast.makeText(this, "senha incorreta!", Toast.LENGTH_SHORT).show()
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

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (event?.keyCode == KeyEvent.KEYCODE_BACK ||
            event?.keyCode == KeyEvent.KEYCODE_HOME ||
            event?.keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            // Consume o evento para evitar que chegue ao sistema operacional
            true
        } else super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Desabilitar outros botões físicos (como volume, energia, etc.)
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            true
        } else super.onKeyDown(keyCode, event)
    }
}