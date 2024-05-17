package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    // Declaração das variáveis de entrada de texto e botões
    private lateinit var userNameView: TextView
    private lateinit var userName: TextView
    private lateinit var emailView: TextView
    private lateinit var secretPassword: TextView
    private lateinit var btnBack: Button
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    // Declaração das instâncias do Firebase Authentication e Firestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        // Inicialização das instâncias do Firebase Authentication e Firestore
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        // Inicializa as variáveis de entrada de texto
        userNameView = findViewById(R.id.userNameView)
        userName = findViewById(R.id.username_view)
        emailView = findViewById(R.id.email_view)
        secretPassword = findViewById(R.id.secretPassword_view)

        // Obter o ID do usuário atualmente autenticado
        val userId = mAuth.currentUser?.uid

        // Recuperar os dados do perfil do usuário do Cloud Firestore
        database.collection("users").document(userId!!).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("username")
                    val email = document.getString("email")
                    val keySecret = document.getString("secretPassword")

                    // Exibir os dados do perfil do usuário na interface do usuário
                    userNameView.text = name
                    userName.text = name
                    emailView.text = email
                    secretPassword.text = keySecret
                } else {
                    userNameView.text = "Dados não encontrados"
                    userName.text = "Dados não encontrados"
                    emailView.text = "Dados não encontrados"
                    secretPassword.text = "Dados não encontrados"
                }
            }

        btnBack = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnEdit = findViewById(R.id.btn_edit)
        btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnLogout = findViewById(R.id.logout_btn)
        btnLogout.setOnClickListener {
            // Deslogar o usuário do FirebaseAuth
            mAuth.signOut()

            startActivity(Intent(this, LoginActivity::class.java))

            // Limpar a pilha de atividades e evitar que o usuário volte para a tela de perfil
            finishAffinity()
        }
    }
}