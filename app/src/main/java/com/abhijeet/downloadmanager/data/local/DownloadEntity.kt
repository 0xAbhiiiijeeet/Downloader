package com.abhijeet.downloadmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abhijeet.downloadmanager.domain.model.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val url: String,
    val fileName: String,
    val filePath: String?,
    val mimeType: String?,
    val progress: Int,
    val status: String,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val errorMessage: String?,
    val workId: String?,
)
