package com.abhijeet.downloadmanager.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class NotificationPermissionHandler (
    private val context: Context,
){
    fun permission(): String = Manifest.permission.POST_NOTIFICATIONS

    fun shouldRequestPermission(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isGranted(): Boolean {
        if(!shouldRequestPermission()) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
