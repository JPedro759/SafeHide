package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    lateinit var btnChangePassword: Button
    lateinit var btnBack: Button

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)

        newPasswordInput = findViewById(R.id.newPassword_input)
        confirmPasswordInput = findViewById(R.id.confirmPassword_input)
        btnBack = findViewById(R.id.btn_back)

        db = FirebaseFirestore.getInstance()

        btnChangePassword = findViewById(R.id.changePassword_btn)
        btnChangePassword.setOnClickListener {

            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()){
                if (newPassword == confirmPassword) {
                    val userId = intent.getStringExtra("userId") // Passando o ID do usuário para esta atividade ao abrir a tela

                    if (userId != null) {
                        db.collection("users").document(userId).update("password", newPassword)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Senha trocada com sucesso!", Toast.LENGTH_SHORT).show()
                                // Após a atualização bem sucedida, o usuário é direcionado de volta para a tela de login
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Falha ao trocar a senha: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}