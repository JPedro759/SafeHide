package com.fatecrl.safehide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.adapter.FileAdapter
import com.fatecrl.safehide.adapter.ImageDeleteListener
import com.fatecrl.safehide.fragments.FileListFragment

class HomeActivity : AppCompatActivity(), ImageDeleteListener {

    private val pickImages: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val imageUri = data?.data
            if (imageUri != null) {
                fileAdapter.addImage(imageUri, applicationContext)
            }
        }
    }

    lateinit var buttonHide: Button
    private val fileAdapter = FileAdapter()

    lateinit var buttonProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        buttonHide = findViewById(R.id.buttonHide)

        val fileListFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as FileListFragment

        fileListFragment.setAdapter(fileAdapter)

        fileAdapter.setDeleteListener(this)

        buttonHide.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            pickImages.launch(intent)
        }

        buttonProfile = findViewById(R.id.btn_profile)
        buttonProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onDeleteImage(imageUri: Uri) {
        fileAdapter.removeImage(imageUri, applicationContext)
    }
}