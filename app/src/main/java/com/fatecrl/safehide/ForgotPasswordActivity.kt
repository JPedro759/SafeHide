package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.ForgotPasswordBinding
import com.fatecrl.safehide.services.FirebaseService.auth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnChangePassword.setOnClickListener {
                val email = binding.emailInput.text.toString()

                if (email.isNotEmpty()) sendPasswordResetEmail(email)
                else showMessage("Por favor, preencha o campo de email!")
            }

            btnBack.setOnClickListener {
                startActivity(
                    Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                )
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showMessage("Email de redefinição de senha enviado!")
                    startActivity(
                        Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                    )
                } else {
                    showMessage("Falha ao enviar email de redefinição de senha: ${task.exception?.message}")
                }
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}