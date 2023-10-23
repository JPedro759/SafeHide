package com.fatecrl.safehide.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R
import com.fatecrl.safehide.adapter.TaskAdapter
import com.fatecrl.safehide.adapter.model.TaskItem

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EmptyListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private val taskList = mutableListOf<TaskItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_empty_list, container, false)
        recyclerView = view.findViewById(R.id.imageList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TaskAdapter(taskList)

        // ?

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EmptyListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}