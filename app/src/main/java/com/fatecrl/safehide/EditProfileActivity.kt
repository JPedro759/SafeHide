package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var newNameInput: EditText
    private lateinit var newEmailInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var newPasswordSecretInput: EditText

    lateinit var btnBack : Button
    lateinit var btnEdit : Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        newNameInput = findViewById(R.id.newName_input)
        newEmailInput = findViewById(R.id.newEmail_input)
        newPasswordInput = findViewById(R.id.newPassword_input)
        newPasswordSecretInput = findViewById(R.id.newPasswordSecret_input)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnBack = findViewById(R.id.btn_back)
        btnEdit = findViewById(R.id.btn_edit)

        btnBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ID do usuário atualmente autenticado
        val userId = mAuth.currentUser?.uid

        // Verifique se o usuário está autenticado
        if (userId != null) {
            // Obtenha uma referência ao documento do usuário no Firestore
            val userRef = db.collection("users").document(userId)

            // Obtenha os dados do usuário do Firestore
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtenha os dados do documento
                    val name = document.getString("username")
                    val email = document.getString("email")
                    val password = document.getString("password")
                    val secretPassword = document.getString("secretPassword")

                    // Defina os dados do usuário nos campos EditText
                    newNameInput.setText(name)
                    newEmailInput.setText(email)
                    newPasswordInput.setText(password)
                    newPasswordSecretInput.setText(secretPassword)
                } else {
                    Toast.makeText(this, "usuário não encontrado", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao obter dados do usuário: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_LONG).show()
        }

        btnEdit.setOnClickListener {
            val newName = newNameInput.text.toString()
            val newEmail = newEmailInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val newPasswordSecret = newPasswordSecretInput.text.toString().trim()

            if(newName.isNotEmpty() && newEmail.isNotEmpty() && newPassword.isNotEmpty() && newPasswordSecret.isNotEmpty()){
                if(newPasswordSecret.length >= 6) {
                    // ID do usuário atualmente autenticado
                    val userId = mAuth.currentUser?.uid

                    if (userId != null) {
                        // Atualizar os dados do usuário no Firestore
                        val userRef = db.collection("users").document(userId)
                        val updates = mutableMapOf<String, Any>()

                        updates["name"] = newName
                        updates["email"] = newEmail
                        updates["password"] = newPassword
                        updates["secretPassword"] = newPasswordSecret

                        userRef.update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Dados alterados com sucesso!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, ProfileActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao atualizar dados: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "A senha secreta deve ter 6 números!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
            }
        }
    }
}