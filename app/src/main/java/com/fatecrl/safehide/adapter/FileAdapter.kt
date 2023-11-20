package com.fatecrl.safehide.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R
import com.google.android.material.snackbar.Snackbar
import java.io.File

interface ImageDeleteListener {
    fun onDeleteImage(imageUri: Uri)
}

class FileAdapter : RecyclerView.Adapter<FileAdapter.ImageViewHolder>() {
    private val fileList = mutableListOf<Uri>()
    private var deleteListener: ImageDeleteListener? = null
    private var currentItemView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (position in 0 until fileList.size) {
            val imageUri = fileList[position]
            holder.imageView.setImageURI(imageUri)

            holder.buttonDelete.setOnClickListener {
                Log.d(imageUri.toString(), "Uri: $imageUri")
                Log.d(imageUri.toString(), "Uri List: ${fileList}")

                deleteListener?.onDeleteImage(imageUri)

                currentItemView = holder.itemView // Define o item atual
                showImageDeletedMessage("Imagem removida da lista!")
            }
        }
    }

    private fun showImageDeletedMessage(message: String) {
        currentItemView?.let { view ->
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view
            val messageTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            messageTextView.maxLines = 3
            snackbar.show()
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun addImage(imageUri: Uri, context: Context) {
        if (!fileList.contains(imageUri)) {
            // Salvar a imagem no armazenamento privado
            val savedImageUri = saveImageToInternalStorage(imageUri, context)
            fileList.add(savedImageUri)
            val lastItemPosition = fileList.size - 1
            notifyItemInserted(lastItemPosition)
            Log.d(savedImageUri.toString(), "Saved ImageUri index: ${fileList.indexOf(savedImageUri)}")
            Log.d("File Path", "Path: ${context.filesDir.absolutePath}")
        }
    }

    private fun saveImageToInternalStorage(imageUri: Uri, context: Context): Uri {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val fileName = "image_${System.currentTimeMillis()}.jpg" // Nome do arquivo único
        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(context.getFileStreamPath(fileName))
    }

    fun setDeleteListener(listener: ImageDeleteListener) {
        deleteListener = listener
    }

    fun removeImage(imageUri: Uri, context: Context) {
        val position = fileList.indexOf(imageUri)

        if (position != -1 && position < fileList.size) {
            // Remove o arquivo do armazenamento interno
            val fileToRemove = File(imageUri.path)
            fileToRemove.delete()

            // Remove o item da lista e notifica a mudança
            fileList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.file_img)
        val buttonDelete: Button = itemView.findViewById(R.id.btn_delete)
    }
}
