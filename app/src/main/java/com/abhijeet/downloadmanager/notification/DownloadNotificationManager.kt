package com.abhijeet.downloadmanager.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import com.abhijeet.downloadmanager.MainActivity

class DownloadNotificationManager(
    private val context: Context,
){
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            NotificationConstants.DOWNLOAD_CHANNEL_ID,
            NotificationConstants.DOWNLOAD_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = NotificationConstants.DOWNLOAD_CHANNEL_DESCRIPTION
        }

        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun foregroundInfo(downloadId: Long, fileName: String, progress: Int, indeterminate: Boolean): ForegroundInfo {
        createNotificationChannel()
        val notification = buildProgressNotification(fileName, progress, indeterminate)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId(downloadId),
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(
                notificationId(downloadId),
                notification,
            )
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyCompleted(downloadId: Long, fileName: String) {
        NotificationManagerCompat.from(context).notify(
            notificationId(downloadId),
            baseBuilder(fileName)
                .setContentTitle(fileName)
                .setContentText("Download completed")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setOngoing(false)
                .setProgress(0, 0,false)
                .build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyFailed(downloadId: Long, fileName: String) {
        NotificationManagerCompat.from(context).notify(
            notificationId(downloadId),
            baseBuilder(fileName)
                .setContentTitle(fileName)
                .setContentText("Download failed")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setOngoing(false)
                .build(),
        )
    }



    private fun baseBuilder(fileName: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationConstants.DOWNLOAD_CHANNEL_ID)
            .setContentIntent(createAppPendingIntent())
            .setContentTitle(fileName)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    private fun buildProgressNotification(fileName: String, progress: Int, indeterminate: Boolean): Notification {
        return baseBuilder(fileName)
            .setContentTitle(fileName)
            .setContentText(if (indeterminate) "Downloading..." else "Downloading $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress.coerceIn(0, 100), indeterminate)
            .build()
    }

    private fun createAppPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun notificationId(downloadId: Long) : Int {
        val normalized = downloadId.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return 1000 + normalized
    }
}

