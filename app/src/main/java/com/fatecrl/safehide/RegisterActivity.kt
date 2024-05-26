package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.RegisterBinding
import com.fatecrl.safehide.model.User
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database
import com.google.firebase.database.DatabaseReference

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterBinding
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usersRef = database.reference.child("users")

        binding.apply {
            // Configura o listener do botão de registro
            btnRegister.setOnClickListener {
                // Obtém os valores dos campos de entrada de texto
                val username = binding.usernameInput.text.toString()
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()
                val repeatPassword = binding.repeatPasswordInput.text.toString().trim()

                if (validateFields(username, email, password, repeatPassword)) {
                    registerAccount(username, email, password)
                }
            }

            // Configura o listener do botão de voltar
            btnBack.setOnClickListener {
                startActivity(
                    Intent(this@RegisterActivity, MainActivity::class.java)
                )
            }
        }
    }

    private fun validateFields(username: String, email: String, password: String, repeatPassword: String): Boolean {
        val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()

        return when {
            username.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty() -> {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
                false
            }
            password != repeatPassword -> {
                Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                false
            }
            !emailRegex.matches(email) -> {
                Toast.makeText(this, "Email inválido!", Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun registerAccount(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val userId = user?.uid ?: return@addOnCompleteListener

                    user.sendEmailVerification().addOnSuccessListener {
                        Toast.makeText(this, "Por favor, verifique seu email!", Toast.LENGTH_LONG).show()
                        saveUserData(userId, username, email)
                    }.addOnFailureListener {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Falha ao cadastrar usuário: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserData(userId: String, username: String, email: String) {
        val secretPassword = "" // Senha secreta do usuário (a ser definida)

        val userInfo = User(username, email, secretPassword)

        usersRef.child(userId).setValue(userInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()

                // Redirecionar o usuário para a tela de cadastro da senha secreta
                startActivity(Intent(this, KeySecretPageActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao cadastrar usuário: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}