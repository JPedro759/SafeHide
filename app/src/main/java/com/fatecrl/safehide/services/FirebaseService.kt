package com.fatecrl.safehide.services

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object FirebaseService {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
}

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicialização do Firebase Auth, do Realtime Database e do Cloud Storage
        FirebaseService.auth
        FirebaseService.database
        FirebaseService.storage
    }
}