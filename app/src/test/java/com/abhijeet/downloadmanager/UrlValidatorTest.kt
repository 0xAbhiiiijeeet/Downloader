package com.abhijeet.downloadmanager.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlValidatorTest {

    @Test
    fun `isValid returns true for a well formed http url`() {
        assertTrue(UrlValidator.isValid(" https://example.com/file.pdf "))
    }

    @Test
    fun `isValid returns false for blank and malformed urls`() {
        assertFalse(UrlValidator.isValid(""))
        assertFalse(UrlValidator.isValid("ftp://example.com/file.pdf"))
        assertFalse(UrlValidator.isValid("https://"))
    }

    @Test
    fun `isSupportedDirectDownloadUrl rejects known unsupported hosts`() {
        assertFalse(UrlValidator.isSupportedDirectDownloadUrl("https://youtube.com/watch?v=123"))
        assertFalse(UrlValidator.isSupportedDirectDownloadUrl("https://vimeo.com/123"))
    }

    @Test
    fun `isSupportedDirectDownloadUrl accepts non blocked hosts`() {
        assertTrue(UrlValidator.isSupportedDirectDownloadUrl("https://example.com/video.mp4"))
    }
}

