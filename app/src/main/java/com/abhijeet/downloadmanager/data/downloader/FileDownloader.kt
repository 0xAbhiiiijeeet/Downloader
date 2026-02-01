package com.abhijeet.downloadmanager.data.downloader

import java.io.File

data class DownloadedFile(
    val file: File,
    val mimeType: String?
)

interface FileDownloader {
    suspend fun download(
        url: String,
        destinationFile: File,
        onProgress: suspend (DownloadProgress) -> Unit,
    ): DownloadedFile
}
