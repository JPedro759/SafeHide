package com.fatecrl.safehide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KeySecretPageActivity : AppCompatActivity() {

    private lateinit var keySecret: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    private lateinit var cardCheck: CardView
    private lateinit var btnRegisterKey: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.keysecret_page)

        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        keySecret = findViewById(R.id.keySecret_input)
        cardCheck = findViewById(R.id.card_check)
        btnRegisterKey = findViewById(R.id.registerKey_btn)

        btnRegisterKey.setOnClickListener {
            val keySecretText = keySecret.text.toString()

            if (keySecretText.length >= 6) {
                val currentUser = mAuth.currentUser
                val userId = currentUser?.uid

                if (userId != null) {
                    // Salvar a senha secreta no Firestore
                    mFirestore.collection("users").document(userId)
                        .update("secretPassword", keySecretText)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Senha secreta cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao cadastrar senha secreta: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "A senha secreta deve ter no mínimo 6 caracteres!", Toast.LENGTH_SHORT).show()
            }
        }

        keySecret.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val key_secret = keySecret.text.toString()

                if (key_secret.length >= 6) {
                    cardCheck.setCardBackgroundColor(Color.parseColor("#8CFF5A")) // Verde se tiver 6 ou mais caracteres
                } else {
                    cardCheck.setCardBackgroundColor(Color.parseColor("#DCDCDC")) // Cor padrão caso contrário
                }
            }
        })
    }
}