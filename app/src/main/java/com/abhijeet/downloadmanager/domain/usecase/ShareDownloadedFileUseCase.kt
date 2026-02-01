package com.abhijeet.downloadmanager.domain.usecase

import android.content.Intent
import com.abhijeet.downloadmanager.core.file.FileManager
import com.abhijeet.downloadmanager.core.file.MimeTypeDetector
import com.abhijeet.downloadmanager.core.util.Constants.SHARE_FILE_ERROR_MESSAGE
import com.abhijeet.downloadmanager.domain.model.DownloadItem

class ShareDownloadedFileUseCase(
    private val fileManager: FileManager,
    private val chooserTitle: String = "Share download",
) {
    operator fun invoke(download: DownloadItem): Intent {
        val filePath = requireNotNull(download.filePath) { SHARE_FILE_ERROR_MESSAGE }
        val file = fileManager.getFile(filePath)
        val mimeType = MimeTypeDetector.detectBest(
            url = download.url,
            serverMimeType = download.mimeType,
            file = file
        )
        val shareIntent = fileManager.buildShareIntent(file, mimeType)
        return Intent.createChooser(shareIntent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
