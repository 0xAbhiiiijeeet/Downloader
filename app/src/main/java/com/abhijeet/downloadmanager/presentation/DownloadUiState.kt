package com.abhijeet.downloadmanager.presentation

import com.abhijeet.downloadmanager.domain.model.DownloadItem

data class DownloadUiState(
    val url: String = "",
    val downloads : List<DownloadItem> = emptyList(),
    val isLoading : Boolean = false,
    val errorMessage : String? = null
)
