package com.abhijeet.downloadmanager.core.file

import org.junit.Assert.assertEquals
import org.junit.Test

class FileNameResolverTest {

    private val resolver = FileNameResolver()

    @Test
    fun `resolve returns file name from url path`() {
        val result = resolver.resolve("https://example.com/files/report.pdf", fallbackTimestamp = 123L)

        assertEquals("report.pdf", result)
    }

    @Test
    fun `resolve uses the last non empty path segment`() {
        val result = resolver.resolve("https://example.com/files/", fallbackTimestamp = 123L)

        assertEquals("files", result)
    }

    @Test
    fun `resolve sanitizes invalid filename characters`() {
        val result = resolver.resolve("https://example.com/files/repo:rt?.pdf", fallbackTimestamp = 123L)

        assertEquals("repo_rt", result)
    }

    @Test
    fun `resolve adds extension when mime type is known`() {
        val result = resolver.resolve("https://example.com/files/video", mimeType = "video/mp4", fallbackTimestamp = 123L)

        assertEquals("video.mp4", result)
    }

    @Test
    fun `resolve does not add bin when mime type is unknown`() {
        val result = resolver.resolve("https://example.com/files/unknown", fallbackTimestamp = 123L)

        assertEquals("unknown", result)
    }
}

