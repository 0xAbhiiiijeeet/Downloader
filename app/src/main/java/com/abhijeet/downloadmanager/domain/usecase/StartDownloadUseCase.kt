package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.core.util.Constants.INVALID_URL_MESSAGE
import com.abhijeet.downloadmanager.core.util.Constants.UNSUPPORTED_URL_MESSAGE
import com.abhijeet.downloadmanager.core.util.UrlValidator
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository

class StartDownloadUseCase(
    private val repository: DownloadRepository,
) {
    suspend operator fun invoke(url: String): Long {
        val trimmedUrl = url.trim()
        require(UrlValidator.isValid(trimmedUrl)) { INVALID_URL_MESSAGE }
        require(UrlValidator.isSupportedDirectDownloadUrl(trimmedUrl)) { UNSUPPORTED_URL_MESSAGE }

        val downloadId = repository.createDownload(trimmedUrl)
        repository.enqueueDownload(downloadId)
        return downloadId
    }
}
