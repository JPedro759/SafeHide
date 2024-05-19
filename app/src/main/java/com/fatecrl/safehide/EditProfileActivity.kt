package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.EditProfileBinding
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EditProfileActivity : AppCompatActivity() {

    // Declaração dos componentes da interface do usuário
    private lateinit var binding: EditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                // Mensagem de erro se o usuário não estiver autenticado
                showMessage("Usuário não autenticado")
            }

            // Listener para o botão de editar
            btnEdit.setOnClickListener {
                // Obtém os valores inseridos pelo usuário nos campos EditText
                val newName = newNameInput.text.toString()
                val newEmail = newEmailInput.text.toString().trim()
                val newPassword = newPasswordInput.text.toString().trim()
                val newSecretPassword = newSecretPasswordInput.text.toString().trim()

                if (validateFields(newName, newEmail, newPassword, newSecretPassword)){
                    // Verifica novamente o ID do usuário atualmente autenticado
                    val userId = auth.currentUser?.uid

                    userId?.let {
                        editUser(it, newName, newEmail, newPassword, newSecretPassword)
                    } ?: run {
                        // Mensagem de erro se o usuário não estiver autenticado
                        showMessage("Usuário não autenticado")
                    }
                }
            }
        }
    }

    private fun displayUserData(userId: String) {
        database.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.data?.let { data ->
                        binding.apply {
                            newNameInput.setText(data["username"].toString())
                            newEmailInput.setText(data["email"].toString())
                            newPasswordInput.setText(data["password"].toString())
                            newSecretPasswordInput.setText(data["secretPassword"].toString())
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

    private fun validateFields(newUserName: String, newEmail: String, newPassword: String, newSecretPassword: String): Boolean {
        return when {
            newUserName.isEmpty() && newEmail.isEmpty() && newPassword.isEmpty() && newSecretPassword.isEmpty() -> {
                showMessage("Por favor, preencha todos os campos!")
                false
            }
            newSecretPassword.length < 6 -> {
                showMessage("A senha secreta deve ter 6 números!")
                false
            }
            else -> true
        }
    }

    private fun editUser(userId: String, newUserName: String, newEmail: String, newPassword: String, newSecretPassword: String){
        // Referência ao documento do usuário no Firestore
        val userRef = database.collection("users").document(userId)

        // Mapa de atualizações
        val updates = mutableMapOf<String, Any>()

        // Atualiza os valores no mapa
        updates["username"] = newUserName
        updates["email"] = newEmail
        updates["password"] = newPassword
        updates["secretPassword"] = newSecretPassword

        // Atualiza os dados do usuário no Firestore
        userRef.update(updates)
            .addOnSuccessListener {
                // Mensagem de sucesso e redirecionamento para a tela de perfil
                Toast.makeText(this, "Dados alterados com sucesso!", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this@EditProfileActivity, ProfileActivity::class.java)
                )
            }.addOnFailureListener { e ->
                // Mensagem de erro em caso de falha ao atualizar os dados
                showMessage("Erro ao atualizar dados: ${e.message}")
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}