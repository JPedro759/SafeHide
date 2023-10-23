package com.fatecrl.safehide

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.adapter.FileAdapter

class MainActivity : AppCompatActivity() {

    private val pickImages: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val imageUri = data?.data
            if (imageUri != null) {
                // Adicione a imagem à instância existente do adaptador
                fileAdapter.addImage(imageUri)
            }
        }
    }

    lateinit var buttonHide: Button
    private val fileAdapter = FileAdapter() // Instância do adaptador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonHide = findViewById(R.id.buttonHide)

        val recyclerViewLayout = layoutInflater.inflate(R.layout.fragment_file_list, null)
        // Configure o RecyclerView com o adaptador
        val recyclerView = recyclerViewLayout.findViewById<RecyclerView>(R.id.fileList)
        recyclerView.adapter = fileAdapter

        buttonHide.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            pickImages.launch(intent)
        }
    }
}