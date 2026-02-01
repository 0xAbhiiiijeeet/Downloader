package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.domain.repository.DownloadRepository

class ClearDownloadUseCase(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke() {
        repository.clearDownloads()
    }
}
