package com.fatecrl.safehide

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.ProfilePageBinding
import com.fatecrl.safehide.model.User
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database
import com.fatecrl.safehide.utils.CryptographyUtils.downloadEncryptedFiles

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ProfilePageBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = auth.currentUser?.uid

        binding.apply {

            userId?.let { loadUserProfile(it) }

            setupListeners()
        }
    }
    private fun loadUserProfile(userId: String) {
        val userRef = database.reference.child("users").child(userId)

        userRef.get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    user?.let {
                        val name = it.username
                        val email = it.email
                        val keySecret = it.secretPassword

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

    @RequiresApi(Build.VERSION_CODES.O)
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
                downloadEncryptedFiles(this@ProfileActivity)

                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(
                        this@ProfileActivity,
                        "Arquivos baixados com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()
                }, 5000)
            }

            btnLogout.setOnClickListener {
                auth.signOut()

                finishAffinity()

                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            }
        }
    }
}
