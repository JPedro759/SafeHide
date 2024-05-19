package com.fatecrl.safehide.services

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Serviço Firebase para fornecer instâncias singleton do FirebaseAuth e FirebaseFirestore.
 * Este objeto usa a propriedade 'by lazy' para inicializar as instâncias apenas quando
 * elas são acessadas pela primeira vez, garantindo que os recursos sejam utilizados
 * de maneira eficiente.
 */
object FirebaseService {

    /**
     * Instância singleton de FirebaseAuth.
     * 'by lazy' garante que a instância seja criada apenas quando for acessada pela primeira vez.
     */
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    /**
     * Instância singleton de FirebaseFirestore.
     * 'by lazy' garante que a instância seja criada apenas quando for acessada pela primeira vez.
     */
    val database: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicialize o Firebase Firestore e o Firebase Auth aqui
        FirebaseService.auth
        FirebaseService.database
    }
}