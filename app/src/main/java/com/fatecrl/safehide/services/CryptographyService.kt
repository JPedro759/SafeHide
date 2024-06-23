
package com.fatecrl.safehide.services

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.firestore
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.CountDownLatch
import javax.crypto.AEADBadTagException
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

    @SuppressLint("GetInstance")
    private fun encryptKey(fileKey: SecretKey, masterKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.WRAP_MODE, masterKey)
        return cipher.wrap(fileKey)
    }

    @SuppressLint("GetInstance")
    private fun decryptKey(encryptedKey: ByteArray, masterKey: SecretKey): SecretKey {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.UNWRAP_MODE, masterKey)
        return cipher.unwrap(encryptedKey, "AES", Cipher.SECRET_KEY) as SecretKey
    }

    private fun saveKeyToFirestore(uid: String, encryptedKey: ByteArray, fileName: String) {
        val keyData = hashMapOf(
            "uid" to uid,
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

    fun encryptMediaFiles(fileUris: List<Uri>, context: Context): List<Uri> {
        val encryptedFiles = mutableListOf<Uri>()
        val masterKey = generateMasterKey()
        val uid = auth.currentUser?.uid

        // Salva a chave mestra no Firestore
        if (uid != null) {
            saveMasterKeyToFirestore(uid, masterKey)
        }

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

                val outputFile = fileUri.lastPathSegment?.let { File(context.filesDir, it) }
                outputFile?.parentFile?.mkdirs() // Certifique-se de que o diretório existe
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
                encryptedFiles.add(encryptedUri)

                if (uid != null) {
                    saveKeyToFirestore(
                        uid,
                        encryptedFileKey,
                        "${fileUri.lastPathSegment}"
                    )
                }

                println("Arquivo criptografado com sucesso: $fileUri")
            }
        }

        return encryptedFiles
    }

    private fun getBytesFromUri(uri: Uri, context: Context): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val buffer = ByteArrayOutputStream()
            val data = ByteArray(1024)
            var nRead: Int

            while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
                buffer.write(data, 0, nRead)
            }

            buffer.flush()
            buffer.toByteArray()
        } ?: throw IOException("Unable to open InputStream for URI: $uri")
    }

    fun decryptMediaFiles(fileUris: List<Uri>, context: Context, fileName: String): List<Uri> {
        val decryptedFiles = mutableListOf<Uri>()
        val uid = auth.currentUser?.uid
        val latch = CountDownLatch(fileUris.size)

        CoroutineScope(Dispatchers.IO).launch {
            fileUris.forEach { uri ->
                try {
                    val encryptedBytes = getBytesFromUri(uri, context)

                    // Create a temporary file to store the encrypted data
                    val tempFileName = "encrypted_${System.currentTimeMillis()}"
                    val tempFile = File(context.filesDir, ".encrypted_files/$tempFileName")
                    tempFile.writeBytes(encryptedBytes)

                    Log.d("TAG", "Temp file created: ${tempFile.absolutePath}")

                    // Extract IV from the encrypted file (first 12 bytes)
                    val ivBytes = encryptedBytes.copyOfRange(0, 12)

                    // Store IV securely
                    val secureStorage = SecureStorage(context)
                    secureStorage.storeIv(tempFileName, ivBytes)

                    // Retrieve encrypted key from Firestore
                    val documents = firestore.collection("keys")
                        .whereEqualTo("uid", uid)
                        .whereEqualTo("fileName", fileName)
                        .get().await()

                    if (documents.isEmpty) {
                        Log.e("Firestore", "No documents found for fileName: $fileName")
                        tempFile.delete()
                        latch.countDown()
                        return@launch
                    }

                    for (document in documents) {
                        val encryptedFileKey = document.getString("encryptedKey")?.fromBase64()
                        Log.d("TAG", "Encrypted File Key: ${Base64.encodeToString(encryptedFileKey, Base64.DEFAULT)}")

                        if (encryptedFileKey == null) {
                            Log.e("Firestore", "No encryptedFileKey found for document: ${document.id}")
                            tempFile.delete()
                            latch.countDown()
                            break
                        }

                        val masterKey = getMasterKey(uid!!)
                        Log.d("TAG", "Master Key: ${Base64.encodeToString(masterKey?.encoded, Base64.DEFAULT)}")

                        if (masterKey == null) {
                            Log.e("Firestore", "Master key not found for uid: $uid")
                            tempFile.delete()
                            latch.countDown()
                            break
                        }

                        val secretKey = decryptKey(encryptedFileKey, masterKey)
                        Log.d("TAG", "Decrypted Secret Key: ${Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)}")

                        // Retrieve IV from secure storage
                        val storedIvBytes = secureStorage.getIv(tempFileName)
                        val gcmSpec = GCMParameterSpec(128, storedIvBytes)

                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

                        val outputFile = File(tempFile.path)
                        FileOutputStream(outputFile).use { outputStream ->
                            val buffer = ByteArray(1024)
                            var bytesRead: Int

                            FileInputStream(tempFile).use { inputFile ->
                                inputFile.skip(12) // Skip the IV

                                while (inputFile.read(buffer).also { bytesRead = it } != -1) {
                                    val decryptedBytes = cipher.update(buffer, 0, bytesRead)

                                    if (decryptedBytes != null) outputStream.write(decryptedBytes)
                                }

                                val finalBytes = cipher.doFinal()

                                if (finalBytes != null) outputStream.write(finalBytes)
                            }
                        }

                        val decryptedUri = Uri.fromFile(outputFile)
                        decryptedFiles.add(decryptedUri)
                        tempFile.delete() // Delete the original encrypted file
                    }
                } catch (e: AEADBadTagException) {
                    Log.e("TAG", "AEADBadTagException: Invalid authentication tag", e)
                    latch.countDown()
                } catch (e: Exception) {
                    Log.e("TAG", "Error decrypting file", e)
                    latch.countDown()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        return decryptedFiles
    }

    class SecureStorage(context: Context) {
        private val sharedPreferences = context.getSharedPreferences("secure_storage", Context.MODE_PRIVATE)

        fun storeIv(fileName: String, ivBytes: ByteArray) {
            val editor = sharedPreferences.edit()
            editor.putString(fileName, Base64.encodeToString(ivBytes, Base64.DEFAULT))
            editor.apply()
        }

        fun getIv(fileName: String): ByteArray? {
            val storedIv = sharedPreferences.getString(fileName, null)
            return if (storedIv!= null) {
                Base64.decode(storedIv, Base64.DEFAULT)
            } else {
                null
            }
        }
    }
}
