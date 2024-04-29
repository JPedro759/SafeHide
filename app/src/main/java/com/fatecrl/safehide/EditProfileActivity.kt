package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var newNameInput: EditText
    private lateinit var newEmailInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var newKeySecretInput: EditText

    lateinit var btnBack : Button
    lateinit var btnEdit : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        newNameInput = findViewById(R.id.newName_input)
        newEmailInput = findViewById(R.id.newEmail_input)
        newPasswordInput = findViewById(R.id.newPassword_input)
        newKeySecretInput = findViewById(R.id.newKeySecret_input)

        btnBack = findViewById(R.id.btn_back)
        btnEdit = findViewById(R.id.btn_edit)

        btnBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        btnEdit.setOnClickListener {
            val newName = newNameInput.text.toString()
            val newEmail = newEmailInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val newKeySecret = newKeySecretInput.text.toString()

            if(newName.isNotEmpty() && newEmail.isNotEmpty() && newPassword.isNotEmpty() && newKeySecret.isNotEmpty()){
                if(newKeySecret.length >= 6) {
                    Toast.makeText(this, "Dados alterados com sucesso!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "A senha secreta deve ter 6 números!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}