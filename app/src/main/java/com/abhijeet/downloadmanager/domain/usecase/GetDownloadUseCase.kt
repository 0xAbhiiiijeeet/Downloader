package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow

class GetDownloadUseCase (
    private val repository: DownloadRepository
){
    operator fun invoke(): Flow<List<DownloadItem>> = repository.observeDownloads()
}
