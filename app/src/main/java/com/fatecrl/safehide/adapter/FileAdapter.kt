package com.fatecrl.safehide.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

// Interface para ouvir eventos de deleção de imagem
interface ImageDeleteListener {
    fun onDeleteImage(imageUri: Uri)
}

// Adaptador para gerenciar uma lista de arquivos (imagens) no RecyclerView
class FileAdapter : RecyclerView.Adapter<FileAdapter.ImageViewHolder>() {

    // Lista de URIs das imagens
    private val fileList = mutableListOf<Uri>()

    // Listener para eventos de deleção de imagem
    private var deleteListener: ImageDeleteListener? = null

    // Referência ao item atual da View
    private var currentItemView: View? = null

    // Cria uma nova ViewHolder para uma imagem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ImageViewHolder(view)
    }

    // Vincula uma imagem ao ViewHolder
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (position in 0 until fileList.size) {
            val imageUri = fileList[position]
            holder.imageView.setImageURI(imageUri)

            holder.buttonDelete.setOnClickListener {
                Log.d(imageUri.toString(), "Uri: $imageUri")
                Log.d(imageUri.toString(), "Uri List: ${fileList}")

                // Chama o listener de deleção
                deleteListener?.onDeleteImage(imageUri)

                // Define o item atual
                currentItemView = holder.itemView
                showImageDeletedMessage("Imagem removida da lista!")
            }
        }
    }

    // Mostra uma mensagem ao usuário sobre a deleção de uma imagem
    private fun showImageDeletedMessage(message: String) {
        currentItemView?.let { view ->
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view
            val messageTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            messageTextView.maxLines = 3
            snackbar.show()
        }
    }

    // Retorna o número de itens na lista
    override fun getItemCount(): Int {
        return fileList.size
    }

    // Adiciona uma nova imagem à lista
    fun addImage(imageUri: Uri, context: Context) {
        // Salva a imagem no armazenamento privado
        val savedImageUri = saveImageToInternalStorage(imageUri, context)

        // Adiciona a URI salva à lista e notifica a mudança
        if (!fileList.contains(savedImageUri)) {
            fileList.add(savedImageUri)
            notifyItemInserted(fileList.size - 1)
            Log.d(savedImageUri.toString(), "Saved ImageUri index: ${fileList.indexOf(savedImageUri)}")
            Log.d("File Path", "Path: ${context.filesDir.absolutePath}")
        }
    }

    // Salva a imagem no armazenamento interno e retorna a URI do arquivo salvo
    private fun saveImageToInternalStorage(imageUri: Uri, context: Context): Uri {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Gera um hash SHA-256 para o nome do arquivo
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(byteArray)
        val fileName = "image_${hash.toHexString()}.jpg"

        // Cria o arquivo no armazenamento interno
        val file = context.getFileStreamPath(fileName)

        // Escreve os dados da imagem no arquivo
        if (!file.exists()) {
            val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.write(byteArray)
            outputStream.close()
        }

        return Uri.fromFile(file)
    }

    // Converte um array de bytes para uma string hexadecimal
    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    // Define o listener de deleção de imagens
    fun setDeleteListener(listener: ImageDeleteListener) {
        deleteListener = listener
    }

    // Remove uma imagem da lista e do armazenamento interno
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

    // ViewHolder para a exibição de uma imagem e seu botão de deleção
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.file_img)
        val buttonDelete: Button = itemView.findViewById(R.id.btn_delete)
    }
}
