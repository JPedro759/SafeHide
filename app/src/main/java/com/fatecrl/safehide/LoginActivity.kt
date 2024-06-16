package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.LoginBinding
import com.fatecrl.safehide.services.FirebaseService.auth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnLogin.setOnClickListener {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                if (validateFields(email, password)){
                    loginUser(email, password)
                }
            }

            forgotPasswordLink.setOnClickListener {
                startActivity(
                    Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                )
            }

            // Botão de voltar
            btnBack.setOnClickListener {
                startActivity(
                    Intent(this@LoginActivity, MainActivity::class.java)
                )
            }
        }
    }

    private fun validateFields(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || password.isEmpty() -> {
                Toast.makeText(this, "Por favor, todos os campos!", Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login bem sucedido
                    val user = auth.currentUser

                    if (user != null && user.isEmailVerified) {
                        Toast.makeText(this, "Login bem sucedido!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Por favor, verifique seu email!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Email ou senha incorreta!"
                        else -> "Falha no login! Verifique suas credenciais."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}