package com.abhijeet.downloadmanager.core.file

import android.webkit.MimeTypeMap
import java.io.File
import java.net.URLConnection

//report.pdf
class MimeTypeResolver {
    fun resolveFromFileName(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isBlank()) return URLConnection.guessContentTypeFromName(fileName)

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: URLConnection.guessContentTypeFromName(fileName)
    }

    fun resolveFromFile(file: File): String? = resolveFromFileName(file.name)
}

