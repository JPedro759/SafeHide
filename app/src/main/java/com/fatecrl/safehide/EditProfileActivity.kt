package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.EditProfileBinding
import com.fatecrl.safehide.model.User
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database
import com.google.firebase.database.DatabaseReference

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: EditProfileBinding
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usersRef = database.reference.child("users")

        binding.apply {
            // Listener para o botão de voltar
            btnBack.setOnClickListener {
                startActivity(
                    Intent(this@EditProfileActivity, ProfileActivity::class.java)
                )
            }

            // Obtém o ID do usuário atualmente autenticado
            val userId = auth.currentUser?.uid

            userId?.let {
                displayUserData(it)
            } ?: run {
                showMessage("Usuário não autenticado")
            }

            // Listener para o botão de editar
            btnEdit.setOnClickListener {
                val newName = newNameInput.text.toString()
                val newSecretPassword = newSecretPasswordInput.text.toString().trim()

                if (newSecretPassword.length >= 6){
                    userId?.let {
                        editUser(it, newName, newSecretPassword)
                    } ?: run {
                        showMessage("Usuário não autenticado")
                    }
                }
            }
        }
    }

    private fun displayUserData(userId: String) {
        val userRef = usersRef.child(userId)

        userRef.get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    user?.let {
                        binding.apply {
                            newNameInput.setText(it.username)
                            newSecretPasswordInput.setText(it.secretPassword)
                        }
                    }
                } else {
                    showMessage("Usuário não encontrado")
                }
            }
            .addOnFailureListener { e ->
                showMessage("Erro ao obter dados do usuário: ${e.message}")
            }
    }

    private fun editUser(userId: String, newUserName: String, newSecretPassword: String){
        val updates = mapOf(
            "username" to newUserName,
            "secretPassword" to newSecretPassword
        )

        // Atualiza os dados do usuário no Realtime Database
        usersRef.child(userId).updateChildren(updates)
            .addOnSuccessListener {
                showMessage("Dados alterados com sucesso!")
                startActivity(
                    Intent(this@EditProfileActivity, ProfileActivity::class.java)
                )
            }.addOnFailureListener { e ->
                showMessage("Erro ao atualizar dados: ${e.message}")
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}