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

class KeySecretPageActivity : AppCompatActivity() {

    lateinit var keySecret : EditText
    lateinit var cardCheck : CardView
    lateinit var btnRegisterKey : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.keysecret_page)

        keySecret = findViewById(R.id.keySecret_input)
        cardCheck = findViewById(R.id.card_check)
        btnRegisterKey = findViewById(R.id.registerKey_btn)

        btnRegisterKey.setOnClickListener {
            val key_secret = keySecret.text.toString()

            if (key_secret.length >= 6) {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "A senha está incorreta!", Toast.LENGTH_SHORT).show()
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