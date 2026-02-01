package com.abhijeet.downloadmanager.core.util

import java.net.URI

object UrlValidator {

    private val unsupportedHosts = setOf(
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be",
        "vimeo.com",
        "www.vimeo.com",
    )

    fun isValid(url: String) : Boolean {
        val trimmedUrl = url.trim()
        if(trimmedUrl.isBlank()) return false
        if(!trimmedUrl.startsWith("https://") && !trimmedUrl.startsWith("http")) return false

        return try {
            val uri = URI(trimmedUrl)
            !uri.host.isNullOrBlank()
        } catch (_: Exception){
            false
        }
    }

    fun isSupportedDirectDownloadUrl(url: String) : Boolean {
        val trimmedUrl = url.trim()
        if(!isValid(trimmedUrl)) return false
        return try {
            val uri = URI(trimmedUrl)
            uri.host?.lowercase() !in unsupportedHosts
        } catch (_: Exception) {
            false
        }
    }
}
