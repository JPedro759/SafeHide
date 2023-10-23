package com.fatecrl.safehide.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R

class FileAdapter : RecyclerView.Adapter<FileAdapter.ImageViewHolder>() {
    private val imageList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = imageList[position]
        holder.imageView.setImageURI(Uri.parse(imageUri))
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun addImage(imageUri: String) {
        imageList.add(imageUri)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.file_img)
    }
}