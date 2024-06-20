package com.fatecrl.safehide.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R
import com.fatecrl.safehide.adapter.FileAdapter

class FileListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private val fileAdapter = FileAdapter()

    fun setAdapter(adapter: FileAdapter) {
        recyclerView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file_list, container, false)

        recyclerView = view.findViewById(R.id.fileList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = fileAdapter

        return view
    }
}
