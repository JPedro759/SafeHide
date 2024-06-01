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

// Interface para ouvir eventos de deleção de arquivo
interface FileDeleteListener {
    fun onDeleteFile(fileUri: Uri)
}

// Adaptador para gerenciar uma lista de arquivos no RecyclerView
class FileAdapter : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    // Lista de URIs dos arquivos
    private val fileList = mutableListOf<Uri>()

    private val user = auth.currentUser

    // Listener para eventos de deleção de arquivo
    private var deleteListener: FileDeleteListener? = null

    // Referência ao item atual da View
    private var currentItemView: View? = null

    // Cria uma nova ViewHolder para um arquivo
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return FileViewHolder(view)
    }

    // Vincula um arquivo ao ViewHolder
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        if (position in 0 until fileList.size) {
            val fileUri = fileList[position]

            // Exibir a imagem do arquivo
            holder.imageView.setImageURI(fileUri)

            // Exibir o nome do arquivo
            val fileName = getFileNameFromUri(fileUri)
            holder.fileName.text = fileName

            // Exibir o tamanho do arquivo
            val fileSize = getFileSize(fileUri, holder.itemView.context)
            holder.fileSize.text = fileSize

            holder.buttonDelete.setOnClickListener {
                // Chama o listener de deleção
                deleteListener?.onDeleteFile(fileUri)

                // Define o item atual
                currentItemView = holder.itemView
                showFileDeletedMessage("Arquivo removido da lista!")
            }
        }
    }

    // Método para carregar os arquivos do Firebase Database
    fun loadFilesFromDatabase() {
        user?.let {
            val userFilesRef = database.reference.child("users").child(it.uid).child("files")
            userFilesRef.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Limpa a lista de arquivos antes de adicionar os novos dados
                    fileList.clear()
                    for (fileSnapshot in snapshot.children) {
                        val fileUriString = fileSnapshot.getValue(String::class.java)
                        fileUriString?.let {
                            val fileUri = Uri.parse(fileUriString)
                            fileList.add(fileUri)
                        }
                    }
                    notifyDataSetChanged() // Notifica o adaptador sobre as mudanças
                }

                override fun onCancelled(error: DatabaseError) {
                    // Exibe uma mensagem de erro ao usuário
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

    // Método para obter o nome do arquivo a partir da Uri
    private fun getFileNameFromUri(uri: Uri): String {
        val path = uri.path
        val fileName = path?.substringAfterLast("/")

        return fileName?.substring(0, 20) + "..."
    }

    // Método para obter o tamanho do arquivo a partir da Uri
    private fun getFileSize(uri: Uri, context: Context): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        var fileSizeInKB = 0.0

        inputStream?.use { input ->
            fileSizeInKB = (input.available() / 1024).toDouble()
        }

        return "$fileSizeInKB KB"
    }

    // Mostra uma mensagem ao usuário sobre a deleção de um arquivo
    private fun showFileDeletedMessage(message: String) {
        currentItemView?.let { view ->
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view
            val messageTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

            messageTextView.maxLines = 3

            snackbar.show()
        }
    }

    // Retorna o número de itens na lista
    override fun getItemCount(): Int {
        return fileList.size
    }

    // Define o listener de deleção de arquivo
    fun setDeleteListener(listener: FileDeleteListener) {
        deleteListener = listener
    }

    // Adiciona um novo arquivo à lista
    fun addFile(fileUri: Uri, context: Context) {
        val savedFileUri = saveFileToInternalStorage(fileUri, context)

        if (savedFileUri != null && !fileList.contains(savedFileUri)) {
            fileList.add(savedFileUri)

            // Adicionar ao Firebase Database
            user?.let {
                val userFilesRef = database.reference.child("users").child(it.uid).child("files")
                userFilesRef.push().setValue(savedFileUri.toString())
            }

            notifyItemInserted(fileList.size - 1)
        }
    }

    // Remove um arquivo da lista
    fun removeFile(fileUri: Uri) {
        val position = fileList.indexOf(fileUri)

        if (position != -1 && position < fileList.size) {
            // Remove o arquivo do armazenamento interno
            val fileToRemove = fileUri.path?.let { File(it) }
            fileToRemove?.delete()

            // Remover do Firebase Database
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
                        // Exibe uma mensagem de erro ao usuário
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

    // Salva o arquivo no armazenamento interno e retorna a URI do arquivo salvo
    private fun saveFileToInternalStorage(fileUri: Uri, context: Context): Uri? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)

        inputStream?.use { input ->
            // Gera um hash SHA-256 para o nome do arquivo
            val digest = MessageDigest.getInstance("SHA-256")
            val byteArray = input.readBytes()
            val hash = digest.digest(byteArray)
            val fileName = "file_${hash.toHexString()}"

            // Cria o diretório de arquivos, se ainda não existir
            val directory = File(context.filesDir, "files")

            if (!directory.exists()) directory.mkdirs()

            // Cria o arquivo no armazenamento interno
            val file = File(directory, fileName)
            FileOutputStream(file).use { output ->
                output.write(byteArray)
            }

            return Uri.fromFile(file)
        }

        return null
    }

    // Converte um array de bytes para uma string hexadecimal
    private fun ByteArray.toHexString(): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(size * 2)

        forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }

        return result.toString()
    }

    // ViewHolder para a exibição de um arquivo e seu botão de deleção
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.file_img)
        val fileName: TextView = itemView.findViewById(R.id.file_name)
        val fileSize: TextView = itemView.findViewById(R.id.file_size)
        val buttonDelete: Button = itemView.findViewById(R.id.btn_delete)
    }
}