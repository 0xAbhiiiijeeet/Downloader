package com.abhijeet.downloadmanager.core.file

import android.webkit.MimeTypeMap
import java.io.File
import java.net.URI

/**
 * Single source of truth for MIME type detection.
 *
 * Resolution cascade (highest to lowest confidence):
 * 1. Server Content-Type header
 * 2. URL file extension mapping
 * 3. Saved file extension mapping
 * 4. application/octet-stream (last resort)
 *
 * Never returns null — callers always get a concrete MIME type.
 */
object MimeTypeDetector {

    private val extensionToMime = mapOf(
        "mp4" to "video/mp4",
        "mkv" to "video/x-matroska",
        "mov" to "video/quicktime",
        "webm" to "video/webm",
        "3gp" to "video/3gpp",
        "mp3" to "audio/mpeg",
        "m4a" to "audio/mp4",
        "wav" to "audio/wav",
        "ogg" to "audio/ogg",
        "aac" to "audio/aac",
        "flac" to "audio/flac",
        "pdf" to "application/pdf",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/png",
        "webp" to "image/webp",
        "gif" to "image/gif",
        "bmp" to "image/bmp",
        "zip" to "application/zip",
        "rar" to "application/vnd.rar",
        "7z" to "application/x-7z-compressed",
        "doc" to "application/msword",
        "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "ppt" to "application/vnd.ms-powerpoint",
        "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "txt" to "text/plain",
        "html" to "text/html",
        "htm" to "text/html",
        "css" to "text/css",
        "js" to "application/javascript",
        "json" to "application/json",
        "xml" to "application/xml",
        "csv" to "text/csv",
        "bin" to "application/octet-stream",
        "apk" to "application/vnd.android.package-archive",
    )

    /**
     * Detects MIME type from a URL using the file extension.
     * Returns null when the extension is unknown or missing.
     */
    fun detectFromUrl(url: String): String? {
        // Only trust a real extension on the last path segment. Guessing from the
        // whole URL maps paths ending in "/" (e.g. /download/video/123/) to text/html.
        val lastSegment = runCatching { URI(url).path.orEmpty() }.getOrDefault("")
            .substringAfterLast('/', "")
        if (!lastSegment.contains('.')) return null
        return detectFromExtension(lastSegment.substringAfterLast('.'))
    }

    /**
     * Detects MIME type from a file extension string (e.g. "mp4").
     */
    fun detectFromExtension(extension: String): String? {
        val ext = extension.lowercase()
        return extensionToMime[ext]
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }

    /**
     * Detects MIME type from a saved file's name/extension.
     */
    fun detectFromFile(file: File): String? {
        val name = file.name
        if (!name.contains(".")) return null
        val ext = name.substringAfterLast(".")
        return detectFromExtension(ext)
    }

    /**
     * Best-effort MIME type detection using all available signals.
     * Never returns null — falls back to application/octet-stream.
     */
    fun detectBest(url: String, serverMimeType: String?, file: File?): String {
        // 1. Server Content-Type header (most authoritative)
        val serverMime = serverMimeType
        if (serverMime?.isNotBlank() == true && serverMime != "application/octet-stream") {
            return serverMime
        }

        // 2. URL extension mapping
        val urlMime = detectFromUrl(url)
        if (urlMime?.isNotBlank() == true) {
            return urlMime
        }

        // 3. Saved file extension mapping
        if (file != null) {
            val fileMime = detectFromFile(file)
            if (fileMime?.isNotBlank() == true) {
                return fileMime
            }
        }

        // 4. Last resort
        return "application/octet-stream"
    }

    /**
     * Maps a MIME type to a file extension. Falls back to "bin" when unknown.
     */
    fun extensionForMimeType(mimeType: String?): String {
        return when (mimeType?.lowercase()) {
            "video/mp4" -> "mp4"
            "video/x-matroska" -> "mkv"
            "video/quicktime" -> "mov"
            "video/webm" -> "webm"
            "video/3gpp" -> "3gp"
            "audio/mpeg" -> "mp3"
            "audio/mp4" -> "m4a"
            "audio/wav" -> "wav"
            "audio/ogg" -> "ogg"
            "audio/aac" -> "aac"
            "audio/flac" -> "flac"
            "application/pdf" -> "pdf"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            "image/bmp" -> "bmp"
            "application/octet-stream", null -> "bin"
            else -> {
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType ?: "")
                if (ext?.isNotBlank() == true) ext else "bin"
            }
        }
    }
}
