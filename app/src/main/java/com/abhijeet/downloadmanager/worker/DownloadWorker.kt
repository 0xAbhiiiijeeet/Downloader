package com.abhijeet.downloadmanager.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.abhijeet.downloadmanager.core.file.FileManager
import com.abhijeet.downloadmanager.core.file.MimeTypeDetector
import com.abhijeet.downloadmanager.core.util.Constants.DOWNLOAD_GENERIC_ERROR_MESSAGE
import com.abhijeet.downloadmanager.data.downloader.FileDownloader
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository
import com.abhijeet.downloadmanager.notification.DownloadNotificationManager
import kotlinx.coroutines.CancellationException
import org.koin.core.context.GlobalContext
import java.io.File

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters,
    private val repository: DownloadRepository,
    private val fileDownloader: FileDownloader,
    private val fileManager: FileManager,
    private val notificationManager: DownloadNotificationManager,
) : CoroutineWorker(appContext, params) {

    constructor(
        appContext: Context,
        params: WorkerParameters,
    ) : this(
        appContext = appContext,
        params = params,
        repository = GlobalContext.get().get(),
        fileDownloader = GlobalContext.get().get(),
        fileManager = GlobalContext.get().get(),
        notificationManager = GlobalContext.get().get(),
    )

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val downloadId = inputData.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        if (downloadId == INVALID_DOWNLOAD_ID) {
            return Result.failure()
        }

        val download = repository.getDownloadById(downloadId) ?: return Result.failure()
        val destinationFile = fileManager.createDestinationFile(download.fileName)

        return try {
            repository.markRunning(downloadId)
            setForeground(notificationManager.foregroundInfo(downloadId, download.fileName, download.progress, true))

            val downloadedFile = fileDownloader.download(
                url = download.url,
                destinationFile = destinationFile,
            ) { progress ->
                repository.updateDownloadProgress(
                    downloadId = downloadId,
                    progress = progress.progressPercentage,
                    downloadedBytes = progress.downloadedBytes,
                    totalBytes = progress.totalBytes,
                )
                setForeground(
                    notificationManager.foregroundInfo(
                        downloadId = downloadId,
                        fileName = download.fileName,
                        progress = progress.progressPercentage,
                        indeterminate = progress.totalBytes <= 0,
                    ),
                )
            }

            if (isStopped) throw CancellationException("Work stopped")

            val resolvedMimeType = MimeTypeDetector.detectBest(
                url = download.url,
                serverMimeType = downloadedFile.mimeType,
                file = downloadedFile.file
            )
            val finalFile = ensureCorrectExtension(downloadedFile.file, resolvedMimeType)

            repository.markCompleted(
                downloadId = downloadId,
                filePath = finalFile.absolutePath,
                mimeType = resolvedMimeType,
            )
            notificationManager.notifyCompleted(downloadId, download.fileName)
            Result.success()
        } catch (_: CancellationException) {
            fileManager.deleteFile(destinationFile)
            repository.markCancelled(downloadId)
            Result.failure()
        } catch (throwable: Throwable) {
            fileManager.deleteFile(destinationFile)
            repository.markFailed(downloadId, throwable.message ?: DOWNLOAD_GENERIC_ERROR_MESSAGE)
            notificationManager.notifyFailed(downloadId, download.fileName)
            Result.failure()
        }
    }

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        private const val INVALID_DOWNLOAD_ID = -1L
    }

    private fun ensureCorrectExtension(file: File, mimeType: String): File {
        val currentName = file.name
        val currentExt = if (currentName.contains(".")) currentName.substringAfterLast(".") else ""
        // Replace missing or .bin placeholder extension with the real one
        if (currentExt.isBlank() || currentExt == "bin") {
            val extension = MimeTypeDetector.extensionForMimeType(mimeType)
            val renamed = File(file.parent, if (currentExt.isBlank()) "$currentName.$extension" else currentName.substringBeforeLast(".") + ".$extension")
            return if (file.renameTo(renamed)) renamed else file
        }
        return file
    }
}

