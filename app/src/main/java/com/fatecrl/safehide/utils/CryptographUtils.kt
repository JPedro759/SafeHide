package com.fatecrl.safehide.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.fatecrl.safehide.services.CryptographyService
import com.fatecrl.safehide.services.FirebaseService.storage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.crypto.SecretKey

object CryptographyUtils {
    // Função Auxiliar para Upload
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

    // Função Auxiliar para Download
    private fun downloadFileFromStorage(path: String, localFile: File): Task<File> {
        val storageRef = storage.reference.child(path)

        return storageRef.getFile(localFile).addOnSuccessListener {
            println("File downloaded successfully: ${localFile.absolutePath}")
        }.addOnFailureListener {
            println("File download failed: ${it.message}")
        }.continueWithTask { task ->
            if (task.isSuccessful) {
                Tasks.forResult(localFile)
            } else {
                task.exception?.let { throw it }
                Tasks.forResult(null)
            }
        }
    }

    // Função principal para Upload
    fun uploadEncryptedFiles(fileUris: List<Uri>, context: Context): Task<Void> {
        val encryptedFiles = CryptographyService.encryptMediaFiles(fileUris, context)

        val uploadTasks = encryptedFiles.map { (encryptedUri) ->
            // Upload do arquivo criptografado
            val fileUploadTask = uploadFileToStorage(encryptedUri, "encrypted_files/${UUID.randomUUID()}")

            fileUploadTask
        }

        return Tasks.whenAll(uploadTasks)
    }

    // Função principal para Download
    fun downloadEncryptedFiles(fileUris: List<Uri>): Task<List<File>> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val downloadTasks = fileUris.map { uri ->
            val fileName = uri.lastPathSegment ?: return@map Tasks.forException<Void>(Exception("Invalid file URI"))
            val encryptedFilePath = "encrypted_files/$fileName"
            val localEncryptedFile = File(downloadsDir, fileName)

            downloadFileFromStorage(encryptedFilePath, localEncryptedFile).continueWith { task ->
                if (task.isSuccessful) {
                    println("File decrypted successfully: ${localEncryptedFile.absolutePath}")
                    localEncryptedFile
                } else {
                    println("Failed to download encrypted file: ${task.exception?.message}")
                    null
                }
            }
        }

        return Tasks.whenAll(downloadTasks).continueWith { task ->
            (if (task.isSuccessful) {
                downloadTasks.mapNotNull { it.result }
            } else {
                task.exception?.let { throw it }
                emptyList()
            }) as List<File>?
        }
    }
}