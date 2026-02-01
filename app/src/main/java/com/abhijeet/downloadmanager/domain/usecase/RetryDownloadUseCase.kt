package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.domain.repository.DownloadRepository


class RetryDownloadUseCase(
    private val repository: DownloadRepository,
) {
    suspend operator fun invoke(downloadId: Long) {
        repository.retryDownload(downloadId)
    }
}

