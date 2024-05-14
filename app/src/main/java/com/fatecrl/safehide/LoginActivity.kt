package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var btnLogin: Button
    private lateinit var forgotPassword: TextView
    private lateinit var btnBack: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        btnBack = findViewById(R.id.btn_back)
        btnLogin = findViewById(R.id.login_btn)
        forgotPassword = findViewById(R.id.forgotPasswordLink)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
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
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
            }
        }

        forgotPassword.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }

        btnBack.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }
}