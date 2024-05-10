package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    lateinit var btnBack : Button
    lateinit var btnEdit : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        btnBack = findViewById(R.id.btn_back)
        btnBack.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)) }

        btnEdit = findViewById(R.id.btn_edit)
        btnEdit.setOnClickListener { startActivity(Intent(this, EditProfileActivity::class.java)) }
    }
}