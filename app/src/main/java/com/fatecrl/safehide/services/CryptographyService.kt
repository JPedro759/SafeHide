package com.fatecrl.safehide.services

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptographyService {
    private fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    fun encryptMediaFiles(fileUris: List<Uri>, masterKey: SecretKey, context: Context): List<Pair<Uri, ByteArray>> {
        val encryptedFiles = mutableListOf<Pair<Uri, ByteArray>>()

        fileUris.forEach { fileUri ->
            val inputStream = context.contentResolver.openInputStream(fileUri)
            inputStream?.use { input ->
                val byteArray = input.readBytes()
                val tempFile = File(context.cacheDir, "tempFile")
                tempFile.writeBytes(byteArray)

                val secretKey = generateEncryptionKey()
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = ByteArray(12) // IV de 12 bytes para GCM
                SecureRandom().nextBytes(iv)
                val gcmSpec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

                val outputFile = File(tempFile.path + ".encrypted")
                FileInputStream(tempFile).use { inputFile ->
                    FileOutputStream(outputFile).use { outputStream ->
                        // Primeiro, escrevemos o IV no arquivo de saída
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

                // Remove temp file
                tempFile.delete()
            }
        }

        return encryptedFiles
    }

    fun decryptMediaFiles(fileUris: List<Uri>, masterKey: SecretKey, encryptedKeys: List<ByteArray>, context: Context): List<Uri> {
        val decryptedFiles = mutableListOf<Uri>()

        fileUris.forEachIndexed { index, fileUri ->
            val encryptedFileKey = encryptedKeys[index]
            val inputStream = context.contentResolver.openInputStream(fileUri)
            inputStream?.use { input ->
                val byteArray = input.readBytes()
                val tempFile = File(context.cacheDir, "tempFile.encrypted")
                tempFile.writeBytes(byteArray)

                val secretKey = decryptKey(encryptedFileKey, masterKey)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = ByteArray(12)
                FileInputStream(tempFile).use { inputFile ->
                    inputFile.read(iv) // Lendo o IV do arquivo criptografado

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

                    // Remove temp file
                    tempFile.delete()
                }
            }
        }

        return decryptedFiles
    }

    /*
    fun encryptMediaFile(file: File, masterKey: SecretKey): Pair<File, ByteArray> {
        val secretKey = generateEncryptionKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12) // IV de 12 bytes para GCM
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val outputFile = File(file.path + ".encrypted")

        FileInputStream(file).use { inputFile ->
            FileOutputStream(outputFile).use { outputStream ->
                // Primeiro, escrevemos o IV no arquivo de saída
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

        // Encrypt the file key with the master key
        val encryptedFileKey = encryptKey(secretKey, masterKey)

        return Pair(outputFile, encryptedFileKey)
    }
     */

    /*
    fun decryptMediaFile(file: File, masterKey: SecretKey, encryptedFileKey: ByteArray): File {
        // Decrypt the file key with the master key
        val secretKey = decryptKey(encryptedFileKey, masterKey)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        FileInputStream(file).use { inputFile ->
            val iv = ByteArray(12)
            inputFile.read(iv) // Lendo o IV do arquivo criptografado

            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val outputFile = File(file.path.removeSuffix(".encrypted") + ".decrypted")

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

            return outputFile
        }
    }
     */

    // Esta função criptografa a chave de criptografia do arquivo (fileKey) usando uma chave mestra (masterKey).
    private fun encryptKey(fileKey: SecretKey, masterKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.WRAP_MODE, masterKey)

        return cipher.wrap(fileKey)
    }

    // Esta função descriptografa a chave de criptografia do arquivo (encryptedKey) usando a mesma chave mestra (masterKey).
    private fun decryptKey(encryptedKey: ByteArray, masterKey: SecretKey): SecretKey {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.UNWRAP_MODE, masterKey)

        return cipher.unwrap(encryptedKey, "AES", Cipher.SECRET_KEY) as SecretKey
    }
}