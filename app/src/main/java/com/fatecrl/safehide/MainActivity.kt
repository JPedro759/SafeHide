package com.fatecrl.safehide

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fatecrl.safehide.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnRegister.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, RegisterActivity::class.java)
                )
            }

            loginLink.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, LoginActivity::class.java)
                )
            }
        }
    }
}