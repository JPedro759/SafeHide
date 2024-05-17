package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    // Declaração das variáveis de entrada de texto e botões
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var repeatPasswordInput: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button

    // Declaração das instâncias do Firebase Authentication e Firestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Inicializa as instâncias do Firebase Authentication e Firestore
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        // Inicializa as variáveis de entrada de texto e botões com os respectivos IDs
        usernameInput = findViewById(R.id.username_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        repeatPasswordInput = findViewById(R.id.repeatPassword_input)

        btnRegister = findViewById(R.id.register_btn)
        btnBack = findViewById(R.id.btn_back)

        // Configura o listener do botão de registro
        btnRegister.setOnClickListener {
            // Obtém os valores dos campos de entrada de texto
            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val repeatPassword = repeatPasswordInput.text.toString()
            val secretPassword = "" // Senha secreta do usuário (a ser definida)

            // Verifica se todos os campos estão preenchidos
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
                // Verifica se as senhas coincidem
                if (password == repeatPassword) {
                    // Cria um novo usuário com email e senha
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Obtém o usuário atual e seu ID
                                val currentUser = mAuth.currentUser
                                val userId = currentUser?.uid

                                // Logs para depuração
                                Log.d("TAG", "User ID: $userId")
                                Log.d("TAG", "User: $currentUser")

                                if (userId != null) {
                                    // Cria um mapa com os dados do usuário
                                    val userMap = hashMapOf(
                                        "username" to username,
                                        "email" to email,
                                        "password" to password,
                                        "secretPassword" to secretPassword
                                    )

                                    // Salva os dados do usuário no Cloud Firestore
                                    mFirestore.collection("users").document(userId).set(userMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()

                                            // Redireciona para a página de senha secreta
                                            startActivity(Intent(this, KeySecretPageActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Erro ao cadastrar usuário: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    Toast.makeText(this, "ID do usuário é nulo!", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(this, "Falha ao cadastrar usuário: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
            }
        }

        // Configura o listener do botão de voltar
        btnBack.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }
}