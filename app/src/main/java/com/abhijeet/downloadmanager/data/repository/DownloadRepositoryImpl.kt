package com.abhijeet.downloadmanager.data.repository

import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.abhijeet.downloadmanager.core.file.FileManager
import com.abhijeet.downloadmanager.core.file.FileNameResolver
import com.abhijeet.downloadmanager.core.file.MimeTypeDetector
import com.abhijeet.downloadmanager.core.util.Constants.DEFAULT_FILE_NAME
import com.abhijeet.downloadmanager.data.local.DownloadDao
import com.abhijeet.downloadmanager.data.local.DownloadEntity
import com.abhijeet.downloadmanager.data.mapper.toDomain
import com.abhijeet.downloadmanager.data.mapper.toStorageValue
import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.model.DownloadStatus
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository
import com.abhijeet.downloadmanager.worker.DownloadWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URI
import java.util.UUID
import kotlin.io.resolve

class DownloadRepositoryImpl(
    private val downloadDao: DownloadDao,
    private val workManager: WorkManager,
    private val fileNameResolver: FileNameResolver,
    private val fileManager: FileManager
) : DownloadRepository {
    override fun observeDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.observeDownloads().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun createDownload(url: String): Long {
        val timestamp = System.currentTimeMillis()
        val detectedMime = MimeTypeDetector.detectFromUrl(url)
        val entity = DownloadEntity(
            url = url,
            fileName = fileNameResolver.resolve(url, mimeType = detectedMime, fallbackTimestamp = timestamp),
            filePath = null,
            mimeType = null,
            progress = 0,
            status = DownloadStatus.Queued.toStorageValue(),
            downloadedBytes = 0,
            totalBytes = 0,
            createdAt = timestamp,
            updatedAt = timestamp,
            completedAt = null,
            errorMessage = null,
            workId = null,
        )
        return downloadDao.insertDownload(entity)
    }

    override suspend fun enqueueDownload(downloadId: Long) {
        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(DownloadWorker.KEY_DOWNLOAD_ID to downloadId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .addTag(workName(downloadId))
            .build()

        downloadDao.updateQueued(
            id = downloadId,
            workId = workRequest.id.toString(),
            status = DownloadStatus.Queued.toStorageValue(),
            updatedAt = System.currentTimeMillis(),
        )

        workManager.enqueueUniqueWork(
            workName(downloadId),
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }

    override suspend fun cancelDownload(downloadId: Long) {
        cancelWork(downloadId)
        markCancelled(downloadId)
    }

    override suspend fun retryDownload(downloadId: Long) {
        cancelWork(downloadId)
        val existing = downloadDao.getDownloadById(downloadId) ?: return
        fileManager.deleteIfExists(existing.filePath)
        downloadDao.resetForRetry(
            id = downloadId,
            status = DownloadStatus.Queued.toStorageValue(),
            updatedAt = System.currentTimeMillis(),
        )
        enqueueDownload(downloadId)
    }

    override suspend fun deleteDownload(id: Long) {
        val existing = downloadDao.getDownloadById(id)
        cancelWork(id)
        fileManager.deleteIfExists(existing?.filePath)
        downloadDao.deleteDownloadById(id)
    }

    override suspend fun clearDownloads() {
        downloadDao.getAllDownloads().forEach { entity ->
            cancelWork(entity.id)
            fileManager.deleteIfExists(entity.filePath)
        }
        downloadDao.clearDownloads()
    }

    override suspend fun updateDownloadProgress(
        downloadId: Long,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long,
    ) {
        downloadDao.updateProgress(
            id = downloadId,
            progress = progress,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            updatedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun markRunning(downloadId: Long) {
        downloadDao.updateStatus(
            id = downloadId,
            status = DownloadStatus.Running.toStorageValue(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun markCompleted(downloadId: Long, filePath: String, mimeType: String?) {
        val now = System.currentTimeMillis()
        downloadDao.markCompleted(
            id = downloadId,
            filePath = filePath,
            fileName = java.io.File(filePath).name,
            mimeType = mimeType,
            status = DownloadStatus.Completed.toStorageValue(),
            completedAt = now,
            updatedAt = now,
        )
    }

    override suspend fun markFailed(downloadId: Long, errorMessage: String?) {
        downloadDao.markFailed(
            id = downloadId,
            status = DownloadStatus.Failed.toStorageValue(),
            errorMessage = errorMessage,
            updatedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun markCancelled(downloadId: Long) {
        downloadDao.markCancelled(
            id = downloadId,
            status = DownloadStatus.Cancelled.toStorageValue(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun getDownloadById(downloadId: Long): DownloadItem? {
        return downloadDao.getDownloadById(downloadId)?.toDomain()
    }

    private suspend fun cancelWork(downloadId: Long) {
        val existing = downloadDao.getDownloadById(downloadId)
        val workId = existing?.workId
        if (!workId.isNullOrBlank()) {
            workManager.cancelWorkById(UUID.fromString(workId))
        } else {
            workManager.cancelUniqueWork(workName(downloadId))
        }
    }

    private fun workName(downloadId: Long): String = "download_$downloadId"

}
