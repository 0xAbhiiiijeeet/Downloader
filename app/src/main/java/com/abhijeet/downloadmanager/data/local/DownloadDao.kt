package com.abhijeet.downloadmanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

//CRUD operations
@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeDownloads(): Flow<List<DownloadEntity>>

    @Insert
    suspend fun insertDownload(entity: DownloadEntity) : Long

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("DELETE FROM downloads")
    suspend fun clearDownloads()

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads")
    suspend fun getAllDownloads(): List<DownloadEntity>

    @Query(
        """
        UPDATE downloads
        SET workId = :workId, status = :status, updatedAt = :updatedAt, errorMessage = NULL
        WHERE id = :id
        """
    )
    suspend fun updateQueued(id: Long, workId: String?, status: String, updatedAt: Long)

    @Query(
        """
        UPDATE downloads
        SET status = :status, updatedAt = :updatedAt, errorMessage = NULL
        WHERE id = :id
        """
    )
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long)

    @Query(
        """
        UPDATE downloads
        SET progress = :progress,
            downloadedBytes = :downloadedBytes,
            totalBytes = :totalBytes,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateProgress(
        id: Long,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE downloads
        SET filePath = :filePath,
            fileName = :fileName,
            mimeType = :mimeType,
            status = :status,
            progress = 100,
            completedAt = :completedAt,
            updatedAt = :updatedAt,
            errorMessage = NULL
        WHERE id = :id
        """
    )
    suspend fun markCompleted(
        id: Long,
        filePath: String,
        fileName: String,
        mimeType: String?,
        status: String,
        completedAt: Long,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE downloads
        SET status = :status,
            errorMessage = :errorMessage,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun markFailed(
        id: Long,
        status: String,
        errorMessage: String?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE downloads
        SET status = :status,
            updatedAt = :updatedAt,
            errorMessage = NULL
        WHERE id = :id
        """
    )
    suspend fun markCancelled(id: Long, status: String, updatedAt: Long)

    @Query(
        """
        UPDATE downloads
        SET progress = 0,
            downloadedBytes = 0,
            totalBytes = 0,
            filePath = NULL,
            mimeType = NULL,
            completedAt = NULL,
            errorMessage = NULL,
            workId = NULL,
            status = :status,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun resetForRetry(id: Long, status: String, updatedAt: Long)

}
