package com.fatecrl.safehide.services

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Serviço Firebase para fornecer instâncias singleton do FirebaseAuth e FirebaseDatabase.
 * Este objeto usa a propriedade 'by lazy' para inicializar as instâncias apenas quando
 * elas são acessadas pela primeira vez, garantindo que os recursos sejam utilizados
 * de maneira eficiente.
 */
object FirebaseService {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
}

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicialize o Realtime Database e o Firebase Auth aqui
        FirebaseService.auth
        FirebaseService.database
    }
}