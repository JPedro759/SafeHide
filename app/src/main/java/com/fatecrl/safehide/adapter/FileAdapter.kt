package com.fatecrl.safehide.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R

class FileAdapter : RecyclerView.Adapter<FileAdapter.ImageViewHolder>() {
    private val fileList = mutableListOf<Uri>() // Use Uri em vez de String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = fileList[position]
        holder.imageView.setImageURI(imageUri)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun addImage(imageUri: Uri) {
        fileList.add(imageUri)
        notifyItemInserted(fileList.size - 1) // Notifique apenas a inserção deste item
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.file_img)
    }
}