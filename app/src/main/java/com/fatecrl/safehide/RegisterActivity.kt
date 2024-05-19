package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.RegisterBinding
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // Configura o listener do botão de registro
            btnRegister.setOnClickListener {
                // Obtém os valores dos campos de entrada de texto
                val username = binding.usernameInput.text.toString()
                val email = binding.emailInput.text.toString()
                val password = binding.passwordInput.text.toString()
                val repeatPassword = binding.repeatPasswordInput.text.toString()

                if (validateFields(username, email, password, repeatPassword)) {
                    registerUser(username, email, password)
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
        return when {
            username.isEmpty() && email.isEmpty() && password.isEmpty() && repeatPassword.isEmpty() -> {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
                false
            }
            password != repeatPassword -> {
                Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun registerUser(username: String, email: String, password: String){
        val user = auth.currentUser
        // Verifica se há um usuário atualmente autenticado
        user?.let {
            // Se houver um usuário autenticado, exiba uma mensagem e retorne sem fazer nada
            Toast.makeText(this, "Você já está autenticado como ${it.email}", Toast.LENGTH_LONG).show()
            return@registerUser
        }

        val secretPassword = "" // Senha secreta do usuário (a ser definida)

        // Cria um novo usuário com email e senha
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Obtém o usuário atual
                    val user = auth.currentUser

                    // Logs para depuração
                    Log.d("TAG", "User ID: ${user?.uid}")
                    Log.d("TAG", "User: $user")

                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "password" to password,
                        "secretPassword" to secretPassword
                    )

                    // Salva os dados do usuário no Cloud Firestore
                    user?.let {
                        database.collection("users").document(user.uid).set(userMap)
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
                } else {
                    Toast.makeText(this, "Falha ao cadastrar usuário: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}