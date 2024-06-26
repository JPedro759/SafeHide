
package com.fatecrl.safehide.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.fatecrl.safehide.NoTouchableScreenActivity
import com.fatecrl.safehide.services.CryptographyService.decryptMediaFiles
import com.fatecrl.safehide.services.CryptographyService.encryptMediaFiles
import com.fatecrl.safehide.services.FirebaseService.firestore
import com.fatecrl.safehide.services.FirebaseService.storage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object CryptographyUtils {
    private fun uploadFileToStorage(uri: Uri): StorageTask<UploadTask.TaskSnapshot> {
        val storageRef = storage.reference.child("encrypted_files/${uri.lastPathSegment}")
        val file = File(uri.path!!)
        val metadata = StorageMetadata.Builder()
            .setContentType("application/octet-stream")
            .build()

        val inputStream = FileInputStream(file)

        return storageRef.putStream(inputStream, metadata)
            .addOnSuccessListener {
                println("File uploaded successfully: ${it.metadata?.path}")
            }
            .addOnFailureListener { exception ->
                println("File upload failed: $exception")
            }
    }

    fun uploadEncryptedFiles(fileUris: List<Uri>, context: Context): Task<Void> {
        Log.d("UploadProcess", "Iniciando upload de arquivos criptografados")
        val encryptedFiles = encryptMediaFiles(fileUris, context)
        Log.w("UploadProcess", "Arquivos criptografados: $encryptedFiles")

        val uploadTasks = encryptedFiles.map { encryptedUri ->
            Log.d("UploadProcess", "Uploading file")
            val fileUploadTask = uploadFileToStorage(encryptedUri)

            fileUploadTask.addOnSuccessListener {
                Log.d("UploadProcess", "File uploaded: ${it.metadata?.path}")

            }.addOnFailureListener { exception ->
                Log.e("UploadProcess", "File upload failed: ${exception.message}")
            }
        }

        return Tasks.whenAll(uploadTasks)
    }

    private suspend fun getFilesFromCloudStorage(): List<String> {
        return suspendCoroutine { continuation ->
            val filesList = mutableListOf<String>()

            storage.reference.child("encrypted_files").listAll().addOnSuccessListener { listResult ->
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

    private fun getFileName(fileName: String): String? {
        return try {
            val document = Tasks.await(firestore.collection("keys")
                .whereEqualTo("fileName", fileName)
                .get()).documents.firstOrNull()

            document?.getString("fileName")
        } catch (e: Exception) {
            Log.e("TAG", "Error fetching fileId from Firestore", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun downloadEncryptedFiles(context: Context) {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val tempDir = File(context.filesDir, ".encrypted_files")

        // Cria a pasta temporária se ela não existir
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("DownloadProcess", "Fetching files from cloud storage")
                val fileNames = getFilesFromCloudStorage()
                Log.d("DownloadProcess", "Files to download: $fileNames")

                // Lista os arquivos já presentes na pasta temporária local
                val localFileNames = tempDir.listFiles()?.map { it.name } ?: emptyList()
                Log.d("DownloadProcess", "Local encrypted files: $localFileNames")

                for (fileName in fileNames) {
                    val encryptedFileRef = storage.reference.child("encrypted_files/$fileName")
                    val tempFile = File(tempDir, fileName)

                    // Baixa o arquivo apenas se ele ainda não estiver na pasta temporária local
                    if (!localFileNames.contains(fileName)) {
                        Log.d("DownloadProcess", "Downloading file: $fileName")
                        downloadFileInChunks(encryptedFileRef, tempFile)
                        Log.d("DownloadProcess", "Downloaded file: ${tempFile.absolutePath}")

                        val fileSize = tempFile.length()
                        val fileFormat = getFileFormat(tempFile)
                        Log.d("DownloadProcess", "Downloaded file size: $fileSize bytes")
                        Log.d("DownloadProcess", "Downloaded file format: $fileFormat")
                    }

                    val file = getFileName(fileName)
                    if (file == null) {
                        Log.e("DownloadProcess", "File not found: $fileName")
                        continue
                    }

                    Log.d("DownloadProcess", "Decrypting file: $fileName")
                    val decryptedUris = decryptMediaFiles(listOf(tempFile.toUri()), context, file)
                    Log.d("DownloadProcess", "Decrypted URIs: $decryptedUris")

                    // Move os arquivos descriptografados para a pasta de Downloads
                    decryptedUris.forEach { decryptedUri ->
                        val outputFile = File(downloadDir, fileName)

                        decryptedUri.path?.let { filePath ->
                            val decryptedFile = File(filePath)
                            decryptedFile.copyTo(outputFile, overwrite = true)
                            Log.d("DownloadProcess", "Decrypted file saved to: ${outputFile.absolutePath}")

                            // Verifica o tamanho e formato do arquivo descriptografado
                            val decryptedFileSize = outputFile.length()
                            val decryptedFileFormat = getFileFormat(outputFile)
                            Log.d("DownloadProcess", "Decrypted file size: $decryptedFileSize bytes")
                            Log.d("DownloadProcess", "Decrypted file format: $decryptedFileFormat")
                        }
                    }
                }

                // Limpa a pasta temporária após descriptografar os arquivos
                tempDir.listFiles()?.forEach { it.delete() }
                Log.d("DownloadProcess", "Temporary files deleted")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DownloadProcess", "Error downloading or decrypting files", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun downloadFileInChunks(fileRef: StorageReference, destFile: File, chunkSize: Long = 1024 * 1024) {
        var offset: Long = 0

        destFile.outputStream().use { output ->
            while (true) {
                val range = offset until offset + chunkSize
                try {
                    val bytes = fileRef.getBytes(range.last).await()
                    output.write(bytes)
                    offset += bytes.size
                    if (bytes.size < chunkSize) break // Último bloco foi menor que o chunkSize, download completo
                } catch (e: Exception) {
                    Log.e("TAG", "Error downloading chunk", e)
                    throw e
                }
            }
        }

        // Verifica o tamanho e formato do arquivo baixado
        val fileSize = destFile.length()
        val fileFormat = getFileFormat(destFile)
        Log.d("TAG", "Downloaded file size: $fileSize bytes")
        Log.d("TAG", "Downloaded file format: $fileFormat")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFileFormat(file: File): String {
        return try {
            Files.probeContentType(file.toPath()) ?: "Unknown"
        } catch (e: IOException) {
            "Unknown"
        }
    }
}