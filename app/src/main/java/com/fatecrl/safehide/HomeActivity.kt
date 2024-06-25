package com.fatecrl.safehide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.adapter.FileAdapter
import com.fatecrl.safehide.adapter.FileDeleteListener
import com.fatecrl.safehide.databinding.HomeBinding
import com.fatecrl.safehide.fragments.FileListFragment
import java.io.File

class HomeActivity : AppCompatActivity(), FileDeleteListener {

    private lateinit var binding: HomeBinding
    private val fileAdapter = FileAdapter()

    private val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val fileUri = data?.data

            if (fileUri != null) fileAdapter.addFile(fileUri, applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileListFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as FileListFragment
        fileListFragment.setAdapter(fileAdapter)

        fileAdapter.setDeleteListener(this)
        fileAdapter.loadFilesFromDatabase()

        binding.apply {
            btnHide.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                pickFiles.launch(intent)
            }

            btnProfile.setOnClickListener {
                startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
            }
        }
    }

    override fun onDeleteFile(fileUri: Uri) {
        fileAdapter.removeFile(fileUri)
    }
}
