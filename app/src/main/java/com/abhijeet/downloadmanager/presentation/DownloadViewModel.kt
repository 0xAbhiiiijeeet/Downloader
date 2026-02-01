package com.abhijeet.downloadmanager.presentation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijeet.downloadmanager.core.util.Constants.DEFAULT_FILE_NAME
import com.abhijeet.downloadmanager.core.util.Constants.DOWNLOAD_GENERIC_ERROR_MESSAGE
import com.abhijeet.downloadmanager.core.util.Constants.INVALID_URL_MESSAGE
import com.abhijeet.downloadmanager.core.util.Constants.NOTIFICATION_PERMISSION_MESSAGE
import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.model.DownloadStatus
import com.abhijeet.downloadmanager.domain.usecase.CancelDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.ClearDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.DeleteDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.GetDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.OpenDownloadedFileUseCase
import com.abhijeet.downloadmanager.domain.usecase.RetryDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.ShareDownloadedFileUseCase
import com.abhijeet.downloadmanager.domain.usecase.StartDownloadUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class DownloadViewModel(
    private val startDownloadUseCase: StartDownloadUseCase,
    getDownloadsUseCase: GetDownloadUseCase,
    private val cancelDownloadUseCase: CancelDownloadUseCase,
    private val retryDownloadUseCase: RetryDownloadUseCase,
    private val deleteDownloadUseCase: DeleteDownloadUseCase,
    private val openDownloadedFileUseCase: OpenDownloadedFileUseCase,
    private val shareDownloadedFileUseCase: ShareDownloadedFileUseCase,
    private val clearDownloadsUseCase: ClearDownloadUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DownloadUiState())
    private val mutableIntents = MutableSharedFlow<Intent>()

    val uiState: StateFlow<DownloadUiState> = mutableUiState.asStateFlow()
    val intents: SharedFlow<Intent> = mutableIntents.asSharedFlow()

    init {
        viewModelScope.launch {
            getDownloadsUseCase()
                .catch {
                    mutableUiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = DOWNLOAD_GENERIC_ERROR_MESSAGE,
                        )
                    }
                }
                .collect { downloads ->
                    mutableUiState.update { currentState ->
                        currentState.copy(
                            downloads = downloads,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onEvent(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.UrlChanged -> updateUrl(event.value)
            DownloadEvent.StartDownloadClicked -> startDownload()
            is DownloadEvent.CancelDownloadClicked -> runAction { cancelDownloadUseCase(event.id) }
            is DownloadEvent.RetryDownloadClicked -> runAction { retryDownloadUseCase(event.id) }
            is DownloadEvent.DeleteDownloadClicked -> runAction { deleteDownloadUseCase(event.id) }
            is DownloadEvent.OpenFileClicked -> openFile(event.item)
            is DownloadEvent.ShareFileClicked -> shareFile(event.item)
            is DownloadEvent.PermissionResultReceived -> handlePermissionResult(event.granted)
            DownloadEvent.ClearDownloadsClicked -> runAction { clearDownloadsUseCase() }
            DownloadEvent.ErrorShown -> clearError()
        }
    }

    private fun updateUrl(url: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                url = url,
                errorMessage = null,
            )
        }
    }

    private fun startDownload() {
        runAction(clearUrlOnSuccess = true) {
            startDownloadUseCase(uiState.value.url)
        }
    }

    private fun openFile(item: DownloadItem) {
        viewModelScope.launch {
            runCatching { openDownloadedFileUseCase(item) }
                .onSuccess { mutableIntents.emit(it) }
                .onFailure { throwable ->
                    mutableUiState.update { currentState ->
                        currentState.copy(errorMessage = throwable.message ?: INVALID_URL_MESSAGE)
                    }
                }
        }
    }

    private fun shareFile(item: DownloadItem) {
        viewModelScope.launch {
            runCatching { shareDownloadedFileUseCase(item) }
                .onSuccess { mutableIntents.emit(it) }
                .onFailure { throwable ->
                    mutableUiState.update { currentState ->
                        currentState.copy(errorMessage = throwable.message ?: DOWNLOAD_GENERIC_ERROR_MESSAGE)
                    }
                }
        }
    }

    private fun handlePermissionResult(granted: Boolean) {
        if (granted) {
            clearError()
            return
        }

        mutableUiState.update { currentState ->
            currentState.copy(errorMessage = NOTIFICATION_PERMISSION_MESSAGE)
        }
    }

    private fun clearError() {
        mutableUiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }

    private fun runAction(
        clearUrlOnSuccess: Boolean = false,
        action: suspend () -> Any?,
    ) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }

            runCatching { action() }
                .onSuccess {
                    mutableUiState.update { currentState ->
                        currentState.copy(
                            url = if (clearUrlOnSuccess) "" else currentState.url,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableUiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: DOWNLOAD_GENERIC_ERROR_MESSAGE,
                        )
                    }
                }
        }
    }
}
