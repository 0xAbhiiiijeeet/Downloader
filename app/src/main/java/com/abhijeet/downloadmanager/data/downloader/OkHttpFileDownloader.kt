package com.abhijeet.downloadmanager.data.downloader

import androidx.core.graphics.alpha
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.abhijeet.downloadmanager.core.file.MimeTypeDetector
import java.io.File

class OkHttpFileDownloader(
    private val okHttpClient: OkHttpClient
) : FileDownloader {
    override suspend fun download(
        url: String,
        destinationFile: File,
        onProgress: suspend (DownloadProgress) -> Unit
    ): DownloadedFile = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val call = okHttpClient.newCall(request)
        currentCoroutineContext().job.invokeOnCompletion { cause ->
            if(cause!=null) call.cancel()
        }

        call.execute().use { response ->
            if(!response.isSuccessful) {
                throw IllegalStateException("Download failed with HTTP  ${response.code}")
            }

            val body = response.body
            val totalBytes = body.contentLength().coerceAtLeast(0L)

            destinationFile.outputStream().use { outputStream ->
                body.byteStream().use { inputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesCopied = 0L
                    var bytesRead : Int

                    while (inputStream.read(buffer).also { bytesRead = it } >=0) {
                        currentCoroutineContext().ensureActive()

                        if(bytesRead == 0) continue
                        outputStream.write(buffer,0,bytesRead)
                        bytesCopied += bytesRead

                        val progressPercentage = if(totalBytes>0) {
                            ((bytesCopied * 100)/totalBytes).toInt()
                        } else {
                            0
                        }

                        onProgress (
                            DownloadProgress(
                                downloadedBytes = bytesCopied,
                                totalBytes = totalBytes,
                                progressPercentage = progressPercentage
                            )
                        )
                    }
                    outputStream.flush()
                }
            }

            DownloadedFile (
                file = destinationFile,
                mimeType = MimeTypeDetector.detectBest(
                    url = url,
                    serverMimeType = response.body?.contentType()?.toString(),
                    file = null
                )
            )

        }
    }

}
