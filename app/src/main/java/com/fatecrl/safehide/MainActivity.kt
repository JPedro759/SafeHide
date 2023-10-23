package com.fatecrl.safehide

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.adapter.model.TaskItem
import com.bumptech.glide.Glide
import com.fatecrl.safehide.adapter.TaskAdapter
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class MainActivity : AppCompatActivity() {
    private lateinit var imagePicker: ActivityResultLauncher<Intent>
    private val PICK_IMAGE_REQUEST = 1;
    private val taskList = mutableListOf<TaskItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonHide = findViewById<Button>(R.id.buttonHide)
        val recyclerView = findViewById<RecyclerView>(R.id.imageList)

        // Configure o layout do RecyclerView (no seu caso, LinearLayoutManager)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Crie uma instância do TaskAdapter e configure o RecyclerView com ela
        val taskAdapter = TaskAdapter(taskList)
        recyclerView.adapter = taskAdapter

        // Inicialize o imagePicker
        imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri: Uri? = data.data

                    // Verifique se o Uri não é nulo
                    if (selectedImageUri != null) {
                        // Carregue a imagem do Uri, por exemplo, usando Glide
                        Glide.with(this).asBitmap().load(selectedImageUri).into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                // Adicione a imagem à lista e notifique o adaptador
                                val title = "Title for the image" // Substitua pelo título desejado
                                val taskItem = TaskItem(resource, title)
                                taskList.add(taskItem)
                                taskAdapter.notifyDataSetChanged() // Notifique o adaptador
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Chamado quando a imagem é removida
                            }
                        })
                    }
                }
            }
        }

        buttonHide.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePicker.launch(intent)
        }
    }
}