package com.fatecrl.safehide

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.KeysecretPageBinding
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database

class KeySecretPageActivity : AppCompatActivity() {

    private lateinit var binding: KeysecretPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KeysecretPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnRegisterKey.setOnClickListener {
                val passwordSecretText = passwordSecretInput.text.toString()

                if (passwordSecretText.length >= 6) saveSecretPassword(passwordSecretText)
                else showMessage("A senha secreta deve ter no mínimo 6 caracteres!")
            }

            passwordSecretInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    updateCardCheckColor(s.toString())
                }
            })
        }
    }

    private fun saveSecretPassword(passwordSecretText: String) {
        val user = auth.currentUser

        user?.uid.let {
            val userRef = database.reference.child("users").child(user!!.uid)

            userRef.child("secretPassword").setValue(passwordSecretText)
                .addOnSuccessListener {
                    showMessage("Senha secreta cadastrada com sucesso!")
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                .addOnFailureListener { e ->
                    showMessage("Erro ao cadastrar senha secreta: ${e.message}")
                }
        }
    }

    private fun updateCardCheckColor(passwordSecretText: String) {
        val color = if (passwordSecretText.length >= 6) {
            Color.parseColor("#8CFF5A") // Verde se tiver 6 ou mais caracteres
        } else {
            Color.parseColor("#DCDCDC") // Cor padrão caso contrário
        }

        binding.cardCheck.setCardBackgroundColor(color)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}