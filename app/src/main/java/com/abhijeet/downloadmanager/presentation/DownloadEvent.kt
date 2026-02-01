package com.abhijeet.downloadmanager.presentation

import com.abhijeet.downloadmanager.domain.model.DownloadItem

sealed interface DownloadEvent {
    data class UrlChanged(val value: String) : DownloadEvent
    data object StartDownloadClicked : DownloadEvent
    data class CancelDownloadClicked(val id: Long) : DownloadEvent
    data class RetryDownloadClicked(val id: Long) : DownloadEvent
    data class DeleteDownloadClicked(val id: Long) : DownloadEvent
    data class OpenFileClicked(val item: DownloadItem) : DownloadEvent
    data class ShareFileClicked(val item: DownloadItem) : DownloadEvent
    data class PermissionResultReceived(val granted: Boolean) : DownloadEvent
    data object ClearDownloadsClicked : DownloadEvent
    data object ErrorShown : DownloadEvent
}
