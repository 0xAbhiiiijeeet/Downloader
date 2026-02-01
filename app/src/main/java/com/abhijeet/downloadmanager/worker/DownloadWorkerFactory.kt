package com.abhijeet.downloadmanager.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.abhijeet.downloadmanager.core.file.FileManager
import com.abhijeet.downloadmanager.data.downloader.FileDownloader
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository
import com.abhijeet.downloadmanager.notification.DownloadNotificationManager
import org.koin.core.context.GlobalContext

class DownloadWorkerFactory : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        if (workerClassName != DownloadWorker::class.java.name) return null

        val koin = GlobalContext.get()
        return DownloadWorker(
            appContext = appContext,
            params = workerParameters,
            repository = koin.get<DownloadRepository>(),
            fileDownloader = koin.get<FileDownloader>(),
            fileManager = koin.get<FileManager>(),
            notificationManager = koin.get<DownloadNotificationManager>(),
        )
    }
}

