package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    lateinit var btnChangePassword: Button
    lateinit var btnBack: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)

        newPasswordInput = findViewById(R.id.newPassword_input)
        confirmPasswordInput = findViewById(R.id.confirmPassword_input)
        btnBack = findViewById(R.id.btn_back)

        btnChangePassword = findViewById(R.id.changePassword_btn)
        btnChangePassword.setOnClickListener {

            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()){
                if (newPassword == confirmPassword) {
                    intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                    Toast.makeText(this, "Senha trocada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}