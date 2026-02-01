package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.domain.repository.DownloadRepository

class CancelDownloadUseCase(
    private val repository: DownloadRepository,
) {
    suspend operator fun invoke(downloadId: Long) {
        repository.cancelDownload(downloadId)
    }
}

