package com.abhijeet.downloadmanager.data.downloader

data class DownloadProgress (
    val downloadedBytes: Long,
    val totalBytes: Long,
    val progressPercentage: Int
)
