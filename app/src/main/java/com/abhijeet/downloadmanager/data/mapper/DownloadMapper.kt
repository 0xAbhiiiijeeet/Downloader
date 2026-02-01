package com.abhijeet.downloadmanager.data.mapper

import com.abhijeet.downloadmanager.data.local.DownloadEntity
import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.model.DownloadStatus

fun DownloadEntity.toDomain(): DownloadItem {
    return DownloadItem(
        id = id,
        url = url,
        fileName = fileName,
        filePath = filePath,
        mimeType = mimeType,
        status = status.toDownloadStatus(),
        progress = progress,
        downloadedBytes = downloadedBytes,
        totalBytes = totalBytes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        errorMessage = errorMessage,
        workId = workId,
    )
}

fun DownloadStatus.toStorageValue(): String = name
fun String.toDownloadStatus(): DownloadStatus {
    return when (this) {
        "Downloading" -> DownloadStatus.Running
        else -> DownloadStatus.entries.firstOrNull { it.name == this } ?: DownloadStatus.Queued
    }
}

