package com.abhijeet.downloadmanager.domain.usecase

import android.content.Intent
import android.content.pm.PackageManager
import com.abhijeet.downloadmanager.core.file.FileManager
import com.abhijeet.downloadmanager.core.file.MimeTypeDetector
import com.abhijeet.downloadmanager.core.util.Constants.OPEN_FILE_ERROR_MESSAGE
import com.abhijeet.downloadmanager.domain.model.DownloadItem

class OpenDownloadedFileUseCase(
    private val fileManager: FileManager,
    private val packageManager: PackageManager,
) {
    operator fun invoke(download: DownloadItem): Intent {
        val filePath = requireNotNull(download.filePath) { OPEN_FILE_ERROR_MESSAGE }
        val file = fileManager.getFile(filePath)
        val mimeType = MimeTypeDetector.detectBest(
            url = download.url,
            serverMimeType = download.mimeType,
            file = file
        )
        val intent = fileManager.buildOpenIntent(file, mimeType)
        check(intent.resolveActivity(packageManager) != null) {
            "No app is available to open this $mimeType file. Install a compatible app (e.g., VLC for videos)."
        }
        return intent
    }
}
