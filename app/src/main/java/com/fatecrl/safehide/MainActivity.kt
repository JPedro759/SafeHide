package com.fatecrl.safehide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSecureFolder = findViewById<Button>(R.id.buttonSecureFolder)

        buttonSecureFolder.setOnClickListener {
            val intent = Intent(this, SecureFolderActivity::class.java)
            startActivity(intent)
        }
    }
}