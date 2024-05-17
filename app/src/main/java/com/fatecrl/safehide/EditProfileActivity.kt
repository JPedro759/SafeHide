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

    // Declaração dos componentes da interface do usuário
    private lateinit var newNameInput: EditText
    private lateinit var newEmailInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var newPasswordSecretInput: EditText

    lateinit var btnBack : Button
    lateinit var btnEdit : Button

    // Declaração da instância do FirebaseAuth para autenticação
    private lateinit var mAuth: FirebaseAuth
    // Declaração da instância do Firestore para armazenamento de dados
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        // Inicialização dos componentes da interface do usuário
        newNameInput = findViewById(R.id.newName_input)
        newEmailInput = findViewById(R.id.newEmail_input)
        newPasswordInput = findViewById(R.id.newPassword_input)
        newPasswordSecretInput = findViewById(R.id.newPasswordSecret_input)

        // Inicialização das instâncias de autenticação e banco de dados
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicialização dos botões
        btnBack = findViewById(R.id.btn_back)
        btnEdit = findViewById(R.id.btn_edit)

        // Listener para o botão de voltar
        btnBack.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Obtém o ID do usuário atualmente autenticado
        val userId = mAuth.currentUser?.uid

        // Verifica se o usuário está autenticado
        if (userId != null) {
            // Referência ao documento do usuário no Firestore
            val userRef = db.collection("users").document(userId)

            // Obtém os dados do usuário do Firestore
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtém os dados do documento
                    val name = document.getString("username")
                    val email = document.getString("email")
                    val password = document.getString("password")
                    val secretPassword = document.getString("secretPassword")

                    // Define os dados do usuário nos campos EditText
                    newNameInput.setText(name)
                    newEmailInput.setText(email)
                    newPasswordInput.setText(password)
                    newPasswordSecretInput.setText(secretPassword)
                } else {
                    // Mensagem de erro se o usuário não for encontrado
                    Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e ->
                // Mensagem de erro em caso de falha ao obter os dados do usuário
                Toast.makeText(this, "Erro ao obter dados do usuário: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            // Mensagem de erro se o usuário não estiver autenticado
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_LONG).show()
        }

        // Listener para o botão de editar
        btnEdit.setOnClickListener {
            // Obtém os valores inseridos pelo usuário nos campos EditText
            val newName = newNameInput.text.toString()
            val newEmail = newEmailInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val newPasswordSecret = newPasswordSecretInput.text.toString().trim()

            // Verifica se todos os campos estão preenchidos
            if (newName.isNotEmpty() && newEmail.isNotEmpty() && newPassword.isNotEmpty() && newPasswordSecret.isNotEmpty()) {
                // Verifica se a senha secreta tem pelo menos 6 caracteres
                if (newPasswordSecret.length >= 6) {
                    // Verifica novamente o ID do usuário atualmente autenticado
                    val userId = mAuth.currentUser?.uid

                    if (userId != null) {
                        // Referência ao documento do usuário no Firestore
                        val userRef = db.collection("users").document(userId)
                        // Mapa de atualizações
                        val updates = mutableMapOf<String, Any>()

                        // Atualiza os valores no mapa
                        updates["username"] = newName
                        updates["email"] = newEmail
                        updates["password"] = newPassword
                        updates["secretPassword"] = newPasswordSecret

                        // Atualiza os dados do usuário no Firestore
                        userRef.update(updates)
                            .addOnSuccessListener {
                                // Mensagem de sucesso e redirecionamento para a tela de perfil
                                Toast.makeText(this, "Dados alterados com sucesso!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, ProfileActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener { e ->
                                // Mensagem de erro em caso de falha ao atualizar os dados
                                Toast.makeText(this, "Erro ao atualizar dados: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        // Mensagem de erro se o usuário não estiver autenticado
                        Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Mensagem de erro se a senha secreta tiver menos de 6 caracteres
                    Toast.makeText(this, "A senha secreta deve ter 6 números!", Toast.LENGTH_LONG).show()
                }
            } else {
                // Mensagem de erro se algum campo não estiver preenchido
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
            }
        }
    }
}