package com.fatecrl.safehide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.adapter.FileAdapter
import com.fatecrl.safehide.adapter.ImageDeleteListener
import com.fatecrl.safehide.databinding.HomeBinding
import com.fatecrl.safehide.fragments.FileListFragment

class HomeActivity : AppCompatActivity(), ImageDeleteListener {

    // Declaração de variáveis para os botões e o adaptador de arquivos
    private lateinit var binding: HomeBinding
    private val fileAdapter = FileAdapter()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtém a referência ao fragmento que lista os arquivos
        val fileListFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as FileListFragment

        // Configura o adaptador para o fragmento de lista de arquivos
        fileListFragment.setAdapter(fileAdapter)

        // Define o listener de deleção de imagens
        fileAdapter.setDeleteListener(this)

        binding.apply {
            // Botão para selecionar imagens da galeria
            btnHide.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                pickImages.launch(intent)
            }

            // Botão para acessar o perfil
            btnProfile.setOnClickListener {
                startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
            }
        }
    }

    // Implementa a função de deleção de imagens da interface ImageDeleteListener
    override fun onDeleteImage(imageUri: Uri) {
        fileAdapter.removeImage(imageUri, applicationContext)
    }
}