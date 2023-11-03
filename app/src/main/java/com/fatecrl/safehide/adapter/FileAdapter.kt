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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R
import com.google.android.material.snackbar.Snackbar

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

    fun addImage(imageUri: Uri) {
        if (!fileList.contains(imageUri)) {
            fileList.add(imageUri)
            val lastItemPosition = fileList.size - 1
            notifyItemInserted(lastItemPosition)
            Log.d(imageUri.toString(), "ImageUri index: ${fileList.indexOf(imageUri)}")
        }
    }

    fun setDeleteListener(listener: ImageDeleteListener) {
        deleteListener = listener
    }

    fun removeImage(imageUri: Uri) {
        val position = fileList.indexOf(imageUri)

        if (position != -1 && position < fileList.size) {
            Log.d(position.toString(), "1 Position: $position")
            fileList.removeAt(position)
            notifyItemRemoved(position)
        }
        Log.d(position.toString(), "2 Position: $position")
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.file_img)
        val buttonDelete: Button = itemView.findViewById(R.id.btn_delete)
    }
}