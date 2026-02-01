package com.abhijeet.downloadmanager.presentation

import android.content.Intent
import com.abhijeet.downloadmanager.core.util.Constants.DOWNLOAD_GENERIC_ERROR_MESSAGE
import com.abhijeet.downloadmanager.core.util.Constants.INVALID_URL_MESSAGE
import com.abhijeet.downloadmanager.core.util.Constants.NOTIFICATION_PERMISSION_MESSAGE
import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.model.DownloadStatus
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository
import com.abhijeet.downloadmanager.domain.usecase.CancelDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.ClearDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.DeleteDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.GetDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.OpenDownloadedFileUseCase
import com.abhijeet.downloadmanager.domain.usecase.RetryDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.ShareDownloadedFileUseCase
import com.abhijeet.downloadmanager.domain.usecase.StartDownloadUseCase
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init collects downloads from repository`() = runTest {
        val repository = ViewModelFakeDownloadRepository()
        val expected = listOf(sampleDownloadItem(id = 7L))
        repository.downloadsFlow.value = expected

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(expected, viewModel.uiState.value.downloads)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `start download success clears input and error`() = runTest {
        val repository = ViewModelFakeDownloadRepository()
        val viewModel = createViewModel(repository)
        viewModel.onEvent(DownloadEvent.UrlChanged("https://example.com/file.zip"))

        viewModel.onEvent(DownloadEvent.StartDownloadClicked)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.url)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf("https://example.com/file.zip"), repository.createdUrls)
        assertEquals(listOf(1L), repository.enqueuedIds)
    }

    @Test
    fun `start download failure exposes validation message`() = runTest {
        val repository = ViewModelFakeDownloadRepository()
        val viewModel = createViewModel(repository)
        viewModel.onEvent(DownloadEvent.UrlChanged("invalid"))

        viewModel.onEvent(DownloadEvent.StartDownloadClicked)
        advanceUntilIdle()

        assertEquals("invalid", viewModel.uiState.value.url)
        assertEquals(INVALID_URL_MESSAGE, viewModel.uiState.value.errorMessage)
        assertEquals(emptyList<String>(), repository.createdUrls)
    }

    @Test
    fun `permission denied sets notification error and granted clears it`() = runTest {
        val repository = ViewModelFakeDownloadRepository()
        val viewModel = createViewModel(repository)

        viewModel.onEvent(DownloadEvent.PermissionResultReceived(false))
        assertEquals(NOTIFICATION_PERMISSION_MESSAGE, viewModel.uiState.value.errorMessage)

        viewModel.onEvent(DownloadEvent.PermissionResultReceived(true))
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `error shown clears current error`() = runTest {
        val repository = ViewModelFakeDownloadRepository()
        val viewModel = createViewModel(repository)
        viewModel.onEvent(DownloadEvent.PermissionResultReceived(false))

        viewModel.onEvent(DownloadEvent.ErrorShown)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `init handles download stream failure`() = runTest {
        val repository = ViewModelFakeDownloadRepository(
            downloadsFlowOverride = flow { throw IllegalStateException("boom") },
        )

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(DOWNLOAD_GENERIC_ERROR_MESSAGE, viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    private fun createViewModel(repository: ViewModelFakeDownloadRepository): DownloadViewModel {
        return DownloadViewModel(
            startDownloadUseCase = StartDownloadUseCase(repository),
            getDownloadsUseCase = GetDownloadUseCase(repository),
            cancelDownloadUseCase = CancelDownloadUseCase(repository),
            retryDownloadUseCase = RetryDownloadUseCase(repository),
            deleteDownloadUseCase = DeleteDownloadUseCase(repository),
            openDownloadedFileUseCase = mockk<OpenDownloadedFileUseCase>(relaxed = true),
            shareDownloadedFileUseCase = mockk<ShareDownloadedFileUseCase>(relaxed = true),
            clearDownloadsUseCase = ClearDownloadUseCase(repository),
        )
    }
}

private class ViewModelFakeDownloadRepository(
    private val downloadsFlowOverride: Flow<List<DownloadItem>>? = null,
) : DownloadRepository {
    val downloadsFlow = MutableStateFlow<List<DownloadItem>>(emptyList())
    val createdUrls = mutableListOf<String>()
    val enqueuedIds = mutableListOf<Long>()

    override fun observeDownloads(): Flow<List<DownloadItem>> = downloadsFlowOverride ?: downloadsFlow

    override suspend fun createDownload(url: String): Long {
        createdUrls += url
        return createdUrls.size.toLong()
    }

    override suspend fun enqueueDownload(downloadId: Long) {
        enqueuedIds += downloadId
    }

    override suspend fun cancelDownload(downloadId: Long) {
        downloadsFlow.update { current -> current.filterNot { it.id == downloadId } }
    }

    override suspend fun retryDownload(downloadId: Long) = Unit

    override suspend fun deleteDownload(id: Long) {
        downloadsFlow.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun clearDownloads() {
        downloadsFlow.value = emptyList()
    }

    override suspend fun updateDownloadProgress(
        downloadId: Long,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long,
    ) = Unit

    override suspend fun markRunning(downloadId: Long) = Unit

    override suspend fun markCompleted(downloadId: Long, filePath: String, mimeType: String?) = Unit

    override suspend fun markFailed(downloadId: Long, errorMessage: String?) = Unit

    override suspend fun markCancelled(downloadId: Long) = Unit

    override suspend fun getDownloadById(downloadId: Long): DownloadItem? {
        return downloadsFlow.value.firstOrNull { it.id == downloadId }
    }
}

private fun sampleDownloadItem(id: Long) = DownloadItem(
    id = id,
    url = "https://example.com/file.zip",
    fileName = "file.zip",
    filePath = null,
    mimeType = null,
    status = DownloadStatus.Queued,
    progress = 0,
    downloadedBytes = 0L,
    totalBytes = 0L,
    createdAt = 0L,
    updatedAt = 0L,
    completedAt = null,
    errorMessage = null,
    workId = null,
)

