package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.ProfilePageBinding
import com.fatecrl.safehide.model.User
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database

class ProfileActivity : AppCompatActivity() {

    // Declaração das variáveis de entrada de texto e botões
    private lateinit var binding: ProfilePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obter o ID do usuário atualmente autenticado
        val userId = auth.currentUser?.uid

        binding.apply {

            userId?.let {         // Se userId não for nulo, executa o bloco let
                loadUserProfile(it)      // Passa o valor de userId (referenciado como it) para loadUserProfile
            }

            setupListeners()
        }
    }
    private fun loadUserProfile(userId: String) {
        // Referência ao nó do usuário específico no Realtime Database
        val userRef = database.reference.child("users").child(userId)

        userRef.get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    // Convertendo o DataSnapshot para o objeto User
                    val user = dataSnapshot.getValue(User::class.java)

                    user?.let {
                        val name = it.username
                        val email = it.email
                        val keySecret = it.secretPassword

                        // Exibir os dados do perfil do usuário na interface do usuário
                        binding.apply {
                            userNameView.text = name
                            nameView.text = name
                            emailView.text = email
                            secretPasswordView.text = keySecret
                        }
                    }
                } else {
                    Toast.makeText(this, "Usuário não encontrado!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Função de baixar os arquivos no dispositivo
    private fun downloadMedias() {
        decryptFiles()
    }

    // Função que descriptografar os arquivos
    private fun decryptFiles() {

    }

    private fun setupListeners() {
        binding.apply{
            btnBack.setOnClickListener {
                startActivity(
                    Intent(this@ProfileActivity, HomeActivity::class.java)
                )
            }

            btnEdit.setOnClickListener {
                startActivity(
                    Intent(this@ProfileActivity, EditProfileActivity::class.java)
                )
            }

            btnDownloadMedia.setOnClickListener {
                downloadMedias()
            }

            btnLogout.setOnClickListener {
                // Deslogar o usuário do FirebasAuth
                auth.signOut()

                startActivity(
                    Intent(this@ProfileActivity, LoginActivity::class.java)
                )

                // Limpar a pilha de atividades e evitar que o usuário volte para a tela de perfil
                finishAffinity()
            }
        }
    }
}
