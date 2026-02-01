package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository


class GetDownloadByIdUseCase(
    private val repository: DownloadRepository,
) {
    suspend operator fun invoke(downloadId: Long): DownloadItem? = repository.getDownloadById(downloadId)
}

