package com.fatecrl.safehide.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fatecrl.safehide.R
import com.fatecrl.safehide.services.FileManager.fileList
import com.fatecrl.safehide.services.FirebaseService.auth
import com.fatecrl.safehide.services.FirebaseService.database
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface FileDeleteListener {
    fun onDeleteFile(fileUri: Uri)
}

class FileAdapter : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {
    private val user = auth.currentUser

    private var deleteListener: FileDeleteListener? = null

    private var currentItemView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        if (position in 0 until fileList.size) {
            val fileUri = fileList[position]

            holder.imageView.setImageURI(fileUri)

            val fileName = getFileNameFromUri(fileUri)
            holder.fileName.text = fileName

            val fileSize = getFileSize(fileUri, holder.itemView.context)
            holder.fileSize.text = fileSize

            holder.buttonDelete.setOnClickListener {
                deleteListener?.onDeleteFile(fileUri)

                currentItemView = holder.itemView
                showFileDeletedMessage("Arquivo removido da lista!")
            }
        }
    }

    fun loadFilesFromDatabase() {
        user?.let {
            val userFilesRef = database.reference.child("users").child(it.uid).child("files")
            userFilesRef.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    fileList.clear()
                    for (fileSnapshot in snapshot.children) {
                        val fileUriString = fileSnapshot.getValue(String::class.java)
                        fileUriString?.let {
                            val fileUri = Uri.parse(fileUriString)
                            fileList.add(fileUri)
                        }
                    }
                    notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    currentItemView?.let { view ->
                        val snackbar = Snackbar.make(view, "Erro ao carregar arquivos: ${error.message}", Snackbar.LENGTH_LONG)
                        val snackbarView = snackbar.view
                        val messageTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                        messageTextView.maxLines = 3

                        snackbar.show()
                    }
                }
            })
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val path = uri.path
        val fileName = path?.substringAfterLast("/")

        return fileName?.substring(0, 20) + "..."
    }

    private fun getFileSize(uri: Uri, context: Context): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        var fileSizeInKB = 0.0

        inputStream?.use { input ->
            fileSizeInKB = (input.available() / 1024).toDouble()
        }

        return "$fileSizeInKB KB"
    }

    private fun showFileDeletedMessage(message: String) {
        currentItemView?.let { view ->
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view
            val messageTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

            messageTextView.maxLines = 3

            snackbar.show()
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun setDeleteListener(listener: FileDeleteListener) {
        deleteListener = listener
    }

    fun addFile(fileUri: Uri, context: Context) {
        val savedFileUri = saveFileToInternalStorage(fileUri, context)

        if (savedFileUri != null && !fileList.contains(savedFileUri)) {
            fileList.add(savedFileUri)

            user?.let {
                val userFilesRef = database.reference.child("users").child(it.uid).child("files")
                userFilesRef.push().setValue(savedFileUri.toString())
            }

            notifyItemInserted(fileList.size - 1)
        }
    }

    fun removeFile(fileUri: Uri) {
        val position = fileList.indexOf(fileUri)

        if (position != -1 && position < fileList.size) {
            // Remove o arquivo do armazenamento interno
            val fileToRemove = fileUri.path?.let { File(it) }
            fileToRemove?.delete()

            user?.let {
                val userFilesRef = database.reference.child("users").child(it.uid).child("files")
                userFilesRef.orderByValue().equalTo(fileUri.toString()).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (fileSnapshot in snapshot.children) {
                            fileSnapshot.ref.removeValue()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        currentItemView?.let { view ->
                            val snackbar = Snackbar.make(view, "Erro ao remover o arquivo: ${error.message}", Snackbar.LENGTH_LONG)
                            val snackbarView = snackbar.view
                            val messageTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                            messageTextView.maxLines = 3

                            snackbar.show()
                        }
                    }
                })
            }

            fileList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun saveFileToInternalStorage(fileUri: Uri, context: Context): Uri? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)

        inputStream?.use { input ->
            val byteArray = input.readBytes()
            val encryptedData = encryptData(byteArray)
            val fileName = generateUniqueFileName()

            val directory = File(context.filesDir, ".files")
            if (!directory.exists()) directory.mkdirs()

            val file = File(directory, fileName)
            FileOutputStream(file).use { output ->
                output.write(encryptedData)
            }

            return Uri.fromFile(file)
        }

        return null
    }

    private fun encryptData(data: ByteArray): ByteArray {
        val secretKey = generateEncryptionKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val encryptedData = cipher.doFinal(data)
        return iv + encryptedData // Prepend the IV to the encrypted data
    }

    private fun generateUniqueFileName(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateEncryptionKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.file_img)
        val fileName: TextView = itemView.findViewById(R.id.file_name)
        val fileSize: TextView = itemView.findViewById(R.id.file_size)
        val buttonDelete: Button = itemView.findViewById(R.id.btn_delete)
    }
}