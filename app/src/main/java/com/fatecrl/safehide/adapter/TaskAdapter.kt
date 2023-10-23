package com.fatecrl.safehide.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R
import com.fatecrl.safehide.adapter.model.TaskItem

class TaskAdapter(private val taskList: List<TaskItem>) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskImage: ImageView = itemView.findViewById(R.id.file_img)
        val taskTitle: TextView = itemView.findViewById(R.id.file_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_empty_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskImage.setImageBitmap(task.imageBitmap)
        holder.taskTitle.text = task.title
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}