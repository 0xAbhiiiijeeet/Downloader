package com.abhijeet.downloadmanager.domain.model

data class DownloadItem(
    val id: Long,
    val url: String,
    val fileName: String,
    val filePath: String?,
    val mimeType: String?,
    val status: DownloadStatus,
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val errorMessage: String?,
    val workId: String?,
)
