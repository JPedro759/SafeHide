package com.fatecrl.safehide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.adapter.FileAdapter
import com.fatecrl.safehide.adapter.ImageDeleteListener
import com.fatecrl.safehide.fragments.FileListFragment

class HomeActivity : AppCompatActivity(), ImageDeleteListener {

    // Inicializa o launcher para selecionar imagens da galeria
    private val pickImages: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val imageUri = data?.data
            if (imageUri != null) {
                fileAdapter.addImage(imageUri, applicationContext)
            }
        }
    }

    // Declaração de variáveis para os botões e o adaptador de arquivos
    lateinit var buttonHide: Button
    private val fileAdapter = FileAdapter()

    lateinit var buttonProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Inicializa o botão para obter imagens da galeria
        buttonHide = findViewById(R.id.buttonHide)

        // Obtém a referência ao fragmento que lista os arquivos
        val fileListFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as FileListFragment

        // Configura o adaptador para o fragmento de lista de arquivos
        fileListFragment.setAdapter(fileAdapter)

        // Define o listener de deleção de imagens
        fileAdapter.setDeleteListener(this)

        // Define a ação do botão para selecionar imagens da galeria
        buttonHide.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            pickImages.launch(intent)
        }

        // Inicializa o botão para acessar o perfil
        buttonProfile = findViewById(R.id.btn_profile)
        buttonProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    // Implementa a função de deleção de imagens da interface ImageDeleteListener
    override fun onDeleteImage(imageUri: Uri) {
        fileAdapter.removeImage(imageUri, applicationContext)
    }
}