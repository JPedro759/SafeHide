package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    // Declaração dos componentes da interface do usuário
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var btnLogin: Button
    private lateinit var forgotPassword: TextView
    private lateinit var btnBack: Button

    // Declaração da Instância do FirebaseAuth para autenticação
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Inicialização da instância do FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Inicialização dos componentes da interface do usuário
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        btnBack = findViewById(R.id.btn_back)
        btnLogin = findViewById(R.id.login_btn)
        forgotPassword = findViewById(R.id.forgotPasswordLink)

        // Listener para o botão de login
        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Obtém o usuário atualmente autenticado
            val currentUser = mAuth.currentUser
            val userId = currentUser?.uid

            // Logs para depuração
            Log.d("TAG", "User ID: $userId")
            Log.d("TAG", "User: $currentUser")

            // Verifica se os campos de email e senha não estão vazios
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Tenta fazer login com o email e a senha fornecidos
                mAuth.signInWithEmailAndPassword(email, password)
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
                                else -> "Falha no login. Verifique suas credenciais."
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                // Mensagem de erro se algum campo não estiver preenchido
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
            }
        }

        // Listener para o link de recuperação de senha
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Listener para o botão de voltar
        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}