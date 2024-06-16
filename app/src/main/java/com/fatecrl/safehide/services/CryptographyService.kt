package com.fatecrl.safehide.services

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.firestore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptographyService (private val context: Context) {
    private fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    private fun generateMasterKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        val secureRandom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong()
        } else {
            SecureRandom()
        }
        keyGenerator.init(256, secureRandom)
        return keyGenerator.generateKey()
    }

    private fun encryptKey(fileKey: SecretKey, masterKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.WRAP_MODE, masterKey)
        return cipher.wrap(fileKey)
    }

    private fun decryptKey(encryptedKey: ByteArray, masterKey: SecretKey): SecretKey {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.UNWRAP_MODE, masterKey)
        return cipher.unwrap(encryptedKey, "AES", Cipher.SECRET_KEY) as SecretKey
    }

    private fun saveKeyToFirestore(uid: String, fileId: String, encryptedKey: ByteArray) {
        val keyData = hashMapOf(
            "uid" to uid,
            "fileId" to fileId,
            "encryptedKey" to encryptedKey.toBase64()
        )

        firestore.collection("keys").add(keyData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    private fun ByteArray.toBase64(): String {
        return Base64.encodeToString(this, Base64.DEFAULT)
    }

    private fun String.fromBase64(): ByteArray {
        return Base64.decode(this, Base64.DEFAULT)
    }

    fun encryptMediaFiles(fileUris: List<Uri>): List<Pair<Uri, ByteArray>> {
        val encryptedFiles = mutableListOf<Pair<Uri, ByteArray>>()
        val masterKey = generateMasterKey()
        val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")
        val uid = user.uid

        fileUris.forEach { fileUri ->
            val inputStream = context.contentResolver.openInputStream(fileUri)
            inputStream?.use { input ->
                val byteArray = input.readBytes()
                val tempFile = File(context.cacheDir, "tempFile")
                tempFile.writeBytes(byteArray)

                val secretKey = generateEncryptionKey()
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = ByteArray(12)
                SecureRandom().nextBytes(iv)
                val gcmSpec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

                val outputFile = File(tempFile.path + ".encrypted")
                FileInputStream(tempFile).use { inputFile ->
                    FileOutputStream(outputFile).use { outputStream ->
                        outputStream.write(iv)
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while (inputFile.read(buffer).also { bytesRead = it } != -1) {
                            val encryptedBytes = cipher.update(buffer, 0, bytesRead)
                            outputStream.write(encryptedBytes)
                        }
                        val finalBytes = cipher.doFinal()
                        outputStream.write(finalBytes)
                    }
                }

                val encryptedFileKey = encryptKey(secretKey, masterKey)
                val encryptedUri = Uri.fromFile(outputFile)
                encryptedFiles.add(encryptedUri to encryptedFileKey)

                // Save the encrypted file key and master key to Firestore
                saveKeyToFirestore(uid, fileUri.lastPathSegment ?: "", encryptedFileKey)

                println("Arquivo criptografado com sucesso: $fileUri")
            }
        }
        return encryptedFiles
    }

    fun decryptMediaFiles(fileUris: List<Uri>, context: Context): List<Uri> {
        val decryptedFiles = mutableListOf<Uri>()
        val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")
        val uid = user.uid

        val latch = CountDownLatch(fileUris.size)

        fileUris.forEach { fileUri ->
            val inputStream = context.contentResolver.openInputStream(fileUri)
            inputStream?.use { input ->
                val byteArray = input.readBytes()
                val tempFile = File(context.cacheDir, "tempFile.encrypted")
                tempFile.writeBytes(byteArray)

                // Retrieve the encrypted file key and master key from Firestore
                firestore.collection("keys")
                    .whereEqualTo("uid", uid)
                    .whereEqualTo("fileId", fileUri.lastPathSegment)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val encryptedFileKey = document.getString("encryptedKey")!!.fromBase64()
                            val secretKey = decryptKey(encryptedFileKey, generateMasterKey())

                            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                            val iv = ByteArray(12)
                            FileInputStream(tempFile).use { inputFile ->
                                inputFile.read(iv)
                                val gcmSpec = GCMParameterSpec(128, iv)
                                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

                                val outputFile = File(tempFile.path.removeSuffix(".encrypted") + ".decrypted")
                                FileOutputStream(outputFile).use { outputStream ->
                                    val buffer = ByteArray(1024)
                                    var bytesRead: Int
                                    while (inputFile.read(buffer).also { bytesRead = it } != -1) {
                                        val decryptedBytes = cipher.update(buffer, 0, bytesRead)
                                        if (decryptedBytes != null) {
                                            outputStream.write(decryptedBytes)
                                        }
                                    }
                                    val finalBytes = cipher.doFinal()
                                    if (finalBytes != null) {
                                        outputStream.write(finalBytes)
                                    }
                                }

                                val decryptedUri = Uri.fromFile(outputFile)
                                decryptedFiles.add(decryptedUri)
                                tempFile.delete()
                            }
                        }
                        latch.countDown()
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Firestore", "Error getting documents: ", exception)
                        latch.countDown()
                    }
            }
        }

        latch.await() // Aguarde até que todas as operações assíncronas sejam concluídas

        return decryptedFiles
    }
}