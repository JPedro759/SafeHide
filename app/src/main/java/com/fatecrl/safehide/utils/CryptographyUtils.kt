package com.fatecrl.safehide.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.fatecrl.safehide.services.CryptographyService.decryptMediaFiles
import com.fatecrl.safehide.services.CryptographyService.encryptMediaFiles
import com.fatecrl.safehide.services.FirebaseService.firestore
import com.fatecrl.safehide.services.FirebaseService.storage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object CryptographyUtils {
    private fun uploadFileToStorage(uri: Uri, path: String): StorageTask<UploadTask.TaskSnapshot> {
        val storageRef = storage.reference.child(path)

        return storageRef.putFile(uri)
            .addOnSuccessListener {
                println("File uploaded successfully: ${it.metadata?.path}")
            }
            .addOnFailureListener { exception ->
                when (exception) {
                    is StorageException -> {
                        when (exception.errorCode) {
                            StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> println("Retry limit exceeded.")
                            StorageException.ERROR_NOT_AUTHORIZED -> println("User not authorized.")
                            StorageException.ERROR_CANCELED -> println("Upload canceled.")
                            StorageException.ERROR_UNKNOWN -> println("Unknown error occurred.")

                            else -> println("Storage exception: ${exception.message}")
                        }
                    }
                    is IOException -> {
                        println("IOException: The server has terminated the upload session")
                    }
                    else -> {
                        println("File upload failed: ${exception.message}")
                    }
                }
            }
    }

    fun uploadEncryptedFiles(fileUris: List<Uri>, context: Context): Task<Void> {
        val encryptedFiles = encryptMediaFiles(fileUris, context)

        val uploadTasks = encryptedFiles.map { (encryptedUri, fileName) ->
            val fileUploadTask = uploadFileToStorage(encryptedUri, "encrypted_files/$fileName")

            fileUploadTask
        }

        return Tasks.whenAll(uploadTasks)
    }

    private suspend fun getFilesFromCloudStorage(): List<String> {
        return suspendCoroutine { continuation ->
            val filesList = mutableListOf<String>()

            storage.reference.child("encrypted_files").listAll().addOnSuccessListener { listResult ->
                Log.d("TAG", "listAll() successful")
                Log.d("TAG", "Number of files found: ${listResult.items.size}")

                if (listResult.items.isEmpty()) {
                    Log.d("TAG", "No files found in Cloud Storage")
                } else {
                    listResult.items.forEach { storageReference ->
                        Log.d("TAG", "File found: ${storageReference.name}")
                        filesList.add(storageReference.name)
                    }
                }

                continuation.resume(filesList)
            }.addOnFailureListener { exception ->
                Log.e("TAG", "listAll() failed", exception)
                exception.printStackTrace()
                continuation.resumeWithException(exception)
            }
        }
    }

    private fun getFileId(fileName: String): String? {
        return try {
            val document = Tasks.await(firestore.collection("keys")
                .whereEqualTo("fileName", fileName)
                .get()).documents.firstOrNull()

            document?.getString("fileId")
        } catch (e: Exception) {
            Log.e("TAG", "Error fetching fileId from Firestore", e)
            null
        }
    }

    fun downloadEncryptedFiles(context: Context) {
        // Diretório de Downloads da galeria
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileNames = getFilesFromCloudStorage()
                Log.d("TAG", "Files to download: $fileNames")

                for (fileName in fileNames) {
                    val encryptedFileRef = storage.reference.child("encrypted_files/$fileName")
                    Log.d("TAG", "File path: $encryptedFileRef")

                    // Fetch the fileId from Firestore
                    val fileId = getFileId(fileName)

                    if (fileId == null) {
                        Log.e("TAG", "File ID not found for fileName: $fileName")
                        continue
                    }

                    Log.d("TAG", "File ID found for fileName: $fileName")

                    // Passa o URI do Cloud Storage diretamente para a função de descriptografia
                    val decryptedUris = decryptMediaFiles(listOf(encryptedFileRef), context, fileId)
                    Log.d("TAG", "Decrypted URIs: $decryptedUris")

                    decryptedUris.forEach { decryptedUri ->
                        val outputFile = File(downloadDir, fileName.removeSuffix(".encrypted"))
                        Log.d("TAG", "Decrypting file: ${decryptedUri.path}")

                        decryptedUri.path?.let { filePath ->
                            val decryptedFile = File(filePath)
                            decryptedFile.copyTo(outputFile, overwrite = true)

                            Log.d("TAG", "Arquivo descriptografado salvo em: ${outputFile.absolutePath}")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "Erro ao obter nomes de arquivos do Cloud Storage", e)
            }
        }
    }
}