package com.abhijeet.downloadmanager.domain.usecase

import com.abhijeet.downloadmanager.core.util.Constants.INVALID_URL_MESSAGE
import com.abhijeet.downloadmanager.core.util.Constants.UNSUPPORTED_URL_MESSAGE
import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StartDownloadUseCaseTest {

    @Test
    fun `invoke trims url then creates and enqueues download`() = runTest {
        val repository = FakeDownloadRepository()
        val useCase = StartDownloadUseCase(repository)

        val downloadId = useCase(" https://example.com/file.zip ")

        assertEquals(1L, downloadId)
        assertEquals(listOf("https://example.com/file.zip"), repository.createdUrls)
        assertEquals(listOf(1L), repository.enqueuedIds)
    }

    @Test
    fun `invoke throws for invalid urls`() = runTest {
        val repository = FakeDownloadRepository()
        val useCase = StartDownloadUseCase(repository)

        val error = runCatching { useCase("not-a-url") }.exceptionOrNull()

        require(error is IllegalArgumentException) { "Expected IllegalArgumentException but was $error" }
        assertEquals(INVALID_URL_MESSAGE, error.message)
        assertEquals(emptyList<String>(), repository.createdUrls)
        assertEquals(emptyList<Long>(), repository.enqueuedIds)
    }

    @Test
    fun `invoke throws for unsupported hosts`() = runTest {
        val repository = FakeDownloadRepository()
        val useCase = StartDownloadUseCase(repository)

        val error = runCatching { useCase("https://youtube.com/watch?v=1") }.exceptionOrNull()

        require(error is IllegalArgumentException) { "Expected IllegalArgumentException but was $error" }
        assertEquals(UNSUPPORTED_URL_MESSAGE, error.message)
        assertEquals(emptyList<String>(), repository.createdUrls)
        assertEquals(emptyList<Long>(), repository.enqueuedIds)
    }
}

private class FakeDownloadRepository : DownloadRepository {
    val createdUrls = mutableListOf<String>()
    val enqueuedIds = mutableListOf<Long>()

    override fun observeDownloads(): Flow<List<DownloadItem>> = emptyFlow()

    override suspend fun createDownload(url: String): Long {
        createdUrls += url
        return createdUrls.size.toLong()
    }

    override suspend fun enqueueDownload(downloadId: Long) {
        enqueuedIds += downloadId
    }

    override suspend fun cancelDownload(downloadId: Long) = Unit

    override suspend fun retryDownload(downloadId: Long) = Unit

    override suspend fun deleteDownload(id: Long) = Unit

    override suspend fun clearDownloads() = Unit

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

    override suspend fun getDownloadById(downloadId: Long): DownloadItem? = null
}

