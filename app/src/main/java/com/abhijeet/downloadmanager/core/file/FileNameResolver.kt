package com.abhijeet.downloadmanager.core.file

import com.abhijeet.downloadmanager.core.util.Constants.DEFAULT_FILE_NAME
import java.net.URI

//www.google.com/download
// downloaded_1_
class FileNameResolver {
    fun resolve(
        url: String,
        mimeType: String? = null,
        fallbackTimestamp: Long = System.currentTimeMillis()
    ): String {
        val pathSegment = runCatching {
            URI(url).path.orEmpty().trimEnd('/').substringAfterLast('/', "")
        }.getOrDefault("")

        var candidate = pathSegment.ifBlank { "${DEFAULT_FILE_NAME}_$fallbackTimestamp" }
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .ifBlank { "${DEFAULT_FILE_NAME}_$fallbackTimestamp" }

        // Only add an extension when we know the MIME type. When unknown,
        // leave the file extensionless; DownloadWorker will add the correct
        // extension once the real MIME type is known after download completes.
        if (mimeType != null && !candidate.contains(".")) {
            val extension = MimeTypeDetector.extensionForMimeType(mimeType)
            candidate = "$candidate.$extension"
        }
        return candidate
    }
}
