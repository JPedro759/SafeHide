package com.fatecrl.safehide.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.fatecrl.safehide.services.CryptographyService
import com.fatecrl.safehide.services.FirebaseService.storage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File
import javax.crypto.SecretKey

object CryptographyUtils {

    // Função Auxiliar para Upload
    private fun uploadFileToStorage(uri: Uri, path: String): StorageTask<UploadTask.TaskSnapshot> {
        val storageRef = storage.reference.child(path)

        return storageRef.putFile(uri).addOnSuccessListener {
            println("File uploaded successfully: ${it.metadata?.path}")
        }.addOnFailureListener {
            println("File upload failed: ${it.message}")
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
    fun uploadEncryptedFiles(fileUris: List<Uri>, fileName: String, masterKey: SecretKey, context: Context): Task<Void> {
        val encryptedFiles = CryptographyService.encryptMediaFiles(fileUris, masterKey, context)

        val uploadTasks = encryptedFiles.map { (encryptedUri, encryptedFileKey) ->
            val encryptedFile = File(encryptedUri.path!!)
            val keyFileName = "$fileName.key"
            val keyFile = File.createTempFile(fileName, ".key").apply {
                writeBytes(encryptedFileKey)
            }

            // Upload the encrypted file and key
            val fileUploadTask = uploadFileToStorage(Uri.fromFile(encryptedFile), "encrypted_files/$fileName")
            val keyUploadTask = uploadFileToStorage(Uri.fromFile(keyFile), "encrypted_files/$keyFileName")

            Tasks.whenAll(fileUploadTask, keyUploadTask)
        }
        return Tasks.whenAll(uploadTasks)
    }

    // Função principal para Download
    fun downloadEncryptedFiles(fileNames: List<String>, masterKey: SecretKey, context: Context): Task<List<File>> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val downloadTasks = fileNames.map { fileName ->
            val encryptedFilePath = "encrypted_files/$fileName.encrypted"
            val keyFilePath = "encrypted_files/$fileName.key"
            val localEncryptedFile = File(downloadsDir, "$fileName.encrypted")
            val localKeyFile = File(downloadsDir, "$fileName.key")

            val keyDownloadTask = downloadFileFromStorage(keyFilePath, localKeyFile)
            keyDownloadTask.continueWithTask { task ->
                if (task.isSuccessful) {
                    val encryptedFileKey = localKeyFile.readBytes()
                    val fileDownloadTask = downloadFileFromStorage(encryptedFilePath, localEncryptedFile)

                    fileDownloadTask.continueWithTask { task2 ->
                        if (task2.isSuccessful) {
                            val decryptedFileUri = CryptographyService.decryptMediaFiles(
                                listOf(Uri.fromFile(localEncryptedFile)),
                                masterKey,
                                listOf(encryptedFileKey),
                                context
                            ).first()
                            val decryptedFile = File(decryptedFileUri.path!!)
                            println("File decrypted successfully: ${decryptedFile.absolutePath}")
                            Tasks.forResult(decryptedFile)
                        } else {
                            task2.exception?.let { throw it }
                            Tasks.forResult<File?>(null)
                        }
                    }
                } else {
                    task.exception?.let { throw it }
                    Tasks.forResult<File?>(null)
                }
            }
        }

        return Tasks.whenAll(downloadTasks).continueWith { task ->
            if (task.isSuccessful) {
                downloadTasks.mapNotNull { it.result }
            } else {
                task.exception?.let { throw it }
                emptyList()
            }
        }
    }


}