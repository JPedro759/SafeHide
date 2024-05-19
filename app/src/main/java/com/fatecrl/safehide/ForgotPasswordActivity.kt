package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.ForgotPasswordBinding
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database

class ForgotPasswordActivity : AppCompatActivity() {

    // Declaração dos componentes da interface do usuário
    private lateinit var binding: ForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // Listener para o botão de alterar senha
            btnChangePassword.setOnClickListener {
                val newPassword = binding.newPasswordInput.text.toString()
                val confirmPassword = binding.confirmPasswordInput.text.toString()

                if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    if (newPassword == confirmPassword) {
                        val userId = auth.currentUser?.uid

                        userId?.let {
                            updatePassword(it, newPassword)
                        } ?: showMessage("ID do usuário é nulo!")
                    } else {
                        showMessage("As senhas não coincidem!")
                    }
                } else {
                    showMessage("Por favor, preencha todos os campos!")
                }
            }

            // Listener para o botão de voltar
            btnBack.setOnClickListener {
                startActivity(
                    Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                )
            }
        }
    }

    private fun updatePassword(userId: String, newPassword: String) {
        database.collection("users").document(userId).update("password", newPassword)
            .addOnSuccessListener {
                showMessage("Senha trocada com sucesso!")
                startActivity(
                    Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                )
            }
            .addOnFailureListener { e ->
                showMessage("Falha ao trocar a senha: ${e.message}")
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}