package com.abhijeet.downloadmanager.data.mapper

import com.abhijeet.downloadmanager.data.local.DownloadEntity
import com.abhijeet.downloadmanager.domain.model.DownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadMapperTest {

    @Test
    fun `toDomain maps entity fields and status`() {
        val entity = DownloadEntity(
            id = 10L,
            url = "https://example.com/file.pdf",
            fileName = "file.pdf",
            filePath = "/tmp/file.pdf",
            mimeType = "application/pdf",
            progress = 80,
            status = "Completed",
            downloadedBytes = 80L,
            totalBytes = 100L,
            createdAt = 1L,
            updatedAt = 2L,
            completedAt = 3L,
            errorMessage = null,
            workId = "work-1",
        )

        val result = entity.toDomain()

        assertEquals(entity.id, result.id)
        assertEquals(entity.url, result.url)
        assertEquals(entity.fileName, result.fileName)
        assertEquals(entity.filePath, result.filePath)
        assertEquals(entity.mimeType, result.mimeType)
        assertEquals(DownloadStatus.Completed, result.status)
        assertEquals(entity.progress, result.progress)
        assertEquals(entity.downloadedBytes, result.downloadedBytes)
        assertEquals(entity.totalBytes, result.totalBytes)
        assertEquals(entity.createdAt, result.createdAt)
        assertEquals(entity.updatedAt, result.updatedAt)
        assertEquals(entity.completedAt, result.completedAt)
        assertEquals(entity.errorMessage, result.errorMessage)
        assertEquals(entity.workId, result.workId)
    }

    @Test
    fun `toDownloadStatus maps legacy Downloading value to Running`() {
        assertEquals(DownloadStatus.Running, "Downloading".toDownloadStatus())
    }

    @Test
    fun `toDownloadStatus falls back to Queued for unknown values`() {
        assertEquals(DownloadStatus.Queued, "Unknown".toDownloadStatus())
    }
}

