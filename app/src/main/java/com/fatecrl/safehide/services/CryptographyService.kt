package com.fatecrl.safehide.services

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.firestore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.CountDownLatch
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptographyService {
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

    private fun saveMasterKeyToFirestore(uid: String, masterKey: SecretKey) {
        val masterKeyData = hashMapOf("uid" to uid, "masterKey" to masterKey.encoded.toBase64())

        firestore.collection("masterKeys").document(uid).set(masterKeyData)
            .addOnSuccessListener {
                Log.d("Firestore", "Master key saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving master key", e)
            }
    }

    private suspend fun getMasterKey(uid: String): SecretKey? = withContext(Dispatchers.IO) {
        return@withContext try {
            val documentSnapshot =
                Tasks.await(firestore.collection("masterKeys").document(uid).get())
            val masterKeyBase64 = documentSnapshot.getString("masterKey")
            if (masterKeyBase64 != null) {
                val masterKeyBytes = masterKeyBase64.fromBase64()
                SecretKeySpec(masterKeyBytes, "AES")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching master key", e)
            null
        }
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

    private fun saveKeyToFirestore(
        uid: String,
        fileId: String,
        encryptedKey: ByteArray,
        fileName: String
    ) {
        val keyData = hashMapOf(
            "uid" to uid,
            "fileId" to fileId,
            "encryptedKey" to encryptedKey.toBase64(),
            "fileName" to fileName
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

    fun encryptMediaFiles(fileUris: List<Uri>, context: Context): List<Pair<Uri, String>> {
        val encryptedFiles = mutableListOf<Pair<Uri, String>>()
        val masterKey = generateMasterKey()
        val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")
        val uid = user.uid

        // Salva a chave mestra no Firestore
        saveMasterKeyToFirestore(uid, masterKey)

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

                val uniqueFileName = UUID.randomUUID().toString()
                val outputFile = File(context.cacheDir, uniqueFileName)
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
                encryptedFiles.add(encryptedUri to uniqueFileName)

                saveKeyToFirestore(
                    uid,
                    fileUri.lastPathSegment ?: "",
                    encryptedFileKey,
                    uniqueFileName
                )

                println("Arquivo criptografado com sucesso: $fileUri")
            }
        }

        return encryptedFiles
    }

    fun decryptMediaFiles(fileUris: List<StorageReference>, context: Context, fileId: String): List<Uri> {
        val decryptedFiles = mutableListOf<Uri>()
        val uid = auth.currentUser?.uid

        val latch = CountDownLatch(fileUris.size)

        CoroutineScope(Dispatchers.IO).launch {
            fileUris.forEach { fileRef ->
                try {
                    val encryptedBytes = fileRef.getBytes(Long.MAX_VALUE).await() // await() para esperar a conclusão assíncrona

                    val tempFile = File.createTempFile("temp", ".encrypted", context.cacheDir)
                    tempFile.writeBytes(encryptedBytes)

                    Log.d("TAG", "Temp file created: ${tempFile.absolutePath}")

                    val documents = firestore.collection("keys")
                        .whereEqualTo("uid", uid)
                        .whereEqualTo("fileId", fileId)
                        .get().await() // await() para esperar a conclusão assíncrona

                    if (documents.isEmpty) {
                        Log.e("Firestore", "No documents found for fileId: $fileId")
                        tempFile.delete()
                        latch.countDown()
                        return@launch
                    }

                    for (document in documents) {
                        val encryptedFileKey = document.getString("encryptedKey")?.fromBase64()
                        if (encryptedFileKey == null) {
                            Log.e(
                                "Firestore",
                                "No encryptedFileKey found for document: ${document.id}"
                            )
                            tempFile.delete()
                            latch.countDown()
                            continue
                        }

                        val masterKey = getMasterKey(uid!!)
                        if (masterKey == null) {
                            Log.e("Firestore", "Master key not found for uid: $uid")
                            tempFile.delete()
                            latch.countDown()
                            continue
                        }

                        val secretKey = decryptKey(encryptedFileKey, masterKey)
                        Log.d("TAG", "Secret key: $secretKey")

                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        val iv = ByteArray(12)
                        FileInputStream(tempFile).use { inputFile ->
                            inputFile.read(iv)
                            val gcmSpec = GCMParameterSpec(128, iv)
                            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

                            val outputFile = File(tempFile.path.removeSuffix(".encrypted"))
                            FileOutputStream(outputFile).use { outputStream ->
                                val buffer = ByteArray(1024)
                                var bytesRead: Int
                                while (inputFile.read(buffer).also { bytesRead = it }!= -1) {
                                    val decryptedBytes = cipher.update(buffer, 0, bytesRead)
                                    if (decryptedBytes!= null) {
                                        outputStream.write(decryptedBytes)
                                    }
                                }
                                val finalBytes = cipher.doFinal()
                                if (finalBytes!= null) {
                                    outputStream.write(finalBytes)
                                }
                            }

                            val decryptedUri = Uri.fromFile(outputFile)
                            decryptedFiles.add(decryptedUri)
                            tempFile.delete() // Delete the original encrypted file
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "Error decrypting file", e)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        return decryptedFiles
    }
}