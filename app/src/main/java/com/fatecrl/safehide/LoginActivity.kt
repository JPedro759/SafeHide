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

    // Declaração dos componentes da interface do usuário
    private lateinit var binding: LoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // Botão de login
            btnLogin.setOnClickListener {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                // Obtém o usuário atualmente autenticado
                val user = auth.currentUser

                // Logs para depuração
                Log.d("TAG", "User ID: ${user?.uid}")
                Log.d("TAG", "User: $user")

                // Verificação da autenticação do usuário
                user?.let {
                    if (validateFields(email, password)){
                        loginUser(email, password)
                    }
                }
            }

            // Link de recuperação de senha
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
            email.isEmpty() -> {
                Toast.makeText(this, "Por favor, insira seu email!", Toast.LENGTH_LONG).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Por favor, insira sua senha!", Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        // Tenta fazer login com o email e a senha fornecidos
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login bem sucedido
                    Toast.makeText(this, "Login bem sucedido!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish() // Encerrar esta atividade para evitar que o usuário retorne a ela usando o botão de voltar
                } else {
                    // Verificar o tipo de erro retornado pela task
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Email ou senha incorreta!"
                        else -> "Falha no login! Verifique suas credenciais."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}