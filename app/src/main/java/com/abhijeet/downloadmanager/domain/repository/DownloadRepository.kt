package com.abhijeet.downloadmanager.domain.repository

import com.abhijeet.downloadmanager.domain.model.DownloadItem
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun observeDownloads(): Flow<List<DownloadItem>>

    suspend fun createDownload(url: String): Long

    suspend fun enqueueDownload(downloadId: Long)

    suspend fun cancelDownload(downloadId: Long)

    suspend fun retryDownload(downloadId: Long)

    suspend fun deleteDownload(id: Long)

    suspend fun clearDownloads()

    suspend fun updateDownloadProgress(
        downloadId: Long,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long,
    )

    suspend fun markRunning(downloadId: Long)

    suspend fun markCompleted(downloadId: Long, filePath: String, mimeType: String?)

    suspend fun markFailed(downloadId: Long, errorMessage: String?)

    suspend fun markCancelled(downloadId: Long)

    suspend fun getDownloadById(downloadId: Long): DownloadItem?
}
