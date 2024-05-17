package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {

    // Declaração dos componentes da interface do usuário
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnBack: Button

    //  Declaração da Instância do Firestore
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)

        // Inicialização dos componentes da interface do usuário
        newPasswordInput = findViewById(R.id.newPassword_input)
        confirmPasswordInput = findViewById(R.id.confirmPassword_input)
        btnBack = findViewById(R.id.btn_back)
        btnChangePassword = findViewById(R.id.changePassword_btn)

        // Inicializa a instância do Firebase
        db = FirebaseFirestore.getInstance()

        // Listener para o botão de alterar senha
        btnChangePassword.setOnClickListener {
            // Obtém os valores dos campos de senha
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Verifica se os campos de senha estão preenchidos
            if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                // Verifica se as senhas coincidem
                if (newPassword == confirmPassword) {
                    // Obtém o ID do usuário passado para esta atividade
                    val userId = intent.getStringExtra("userId")

                    // Verifica se o ID do usuário não é nulo
                    if (userId != null) {
                        // Atualiza a senha no Firestore
                        db.collection("users").document(userId).update("password", newPassword)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Senha trocada com sucesso!", Toast.LENGTH_SHORT).show()
                                // Redireciona para a tela de login após a atualização bem-sucedida
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                            .addOnFailureListener { e ->
                                // Mostra uma mensagem de erro caso a atualização falhe
                                Toast.makeText(this, "Falha ao trocar a senha: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "ID do usuário é nulo!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Mostra uma mensagem de erro caso as senhas não coincidam
                    Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                }
            } else {
                // Mostra uma mensagem de erro caso os campos não estejam preenchidos
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_LONG).show()
            }
        }

        // Listener para o botão de voltar
        btnBack.setOnClickListener {
            // Redireciona para a tela de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}