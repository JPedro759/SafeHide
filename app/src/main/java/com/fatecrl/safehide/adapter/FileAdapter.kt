package com.fatecrl.safehide.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.MainActivity
import com.fatecrl.safehide.R

interface ImageDeleteListener {
    fun onDeleteImage(imageUri: Uri)
}

class FileAdapter : RecyclerView.Adapter<FileAdapter.ImageViewHolder>() {
    private val fileList = mutableListOf<Uri>()
    private var deleteListener: ImageDeleteListener? = null

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
            }
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun addImage(imageUri: Uri) {
        fileList.add(imageUri)
        notifyItemInserted(fileList.size - 1)
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