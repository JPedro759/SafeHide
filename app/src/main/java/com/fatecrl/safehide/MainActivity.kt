package com.fatecrl.safehide

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private lateinit var imagePicker: ActivityResultLauncher<Intent>
    private val PICK_IMAGE_REQUEST = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSecureFolder = findViewById<Button>(R.id.buttonSecureFolder)

        buttonSecureFolder.setOnClickListener {
            val intent = Intent(this, SecureFolderActivity::class.java)
            startActivity(intent)
        }

        val buttonHide = findViewById<Button>(R.id.buttonHide)

        // Inicialize o imagePicker
        imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri: Uri? = data.data
                    // Faça algo com a URI da imagem selecionada.
                }
            }
        }

        buttonHide.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePicker.launch(intent)
        }
    }
}