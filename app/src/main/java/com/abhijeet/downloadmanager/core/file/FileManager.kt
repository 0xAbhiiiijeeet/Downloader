package com.abhijeet.downloadmanager.core.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class FileManager (
    private val context: Context
) {
    private val authority = "${context.packageName}.fileprovider"

    fun createDestinationFile(fileName: String) : File {
        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "",
        ).apply { mkdirs() }

        val baseName = fileName.substringBeforeLast('.', fileName)
        val extension = fileName.substringAfterLast('.', "")
        var candidate = File(directory,fileName)
        var index = 1
        // report_1.pdf
        // report_1.pdf

        while (candidate.exists()) {
            val suffix = "_$index"
            val resolvedName = if(extension.isBlank()) {
                "$baseName$suffix"
            } else {
                "$baseName$suffix.$extension"
            }
            candidate = File(directory, resolvedName)
            index +=1
        }
        return candidate
    }

    fun getFile(path: String) : File = File(path)

    fun deleteIfExists(path: String?) {
        if(path.isNullOrBlank()) return
        deleteFile(File(path))
    }

    fun deleteFile(file:File?) {
        if(file == null) return
        if(file.exists()) {
            file.delete()
        }
    }

    fun getContentUri(file: File): Uri {
        return FileProvider.getUriForFile(context,authority, file)
    }

    fun buildOpenIntent(file: File, mimeType: String?): Intent {
        val type = mimeType ?: "application/octet-stream"
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(getContentUri(file), type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun buildShareIntent(file: File, mimeType: String?) : Intent {
        val type = mimeType ?: "application/octet-stream"
        return Intent(Intent.ACTION_SEND).apply {
            this.type = type
            putExtra(Intent.EXTRA_STREAM, getContentUri(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
