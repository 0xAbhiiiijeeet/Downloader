package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.domain.repository.DownloadRepository

class DeleteDownloadUseCase(
    private val repository: DownloadRepository
){
    suspend operator fun invoke(id: Long){
        repository.deleteDownload(id)
    }
}
