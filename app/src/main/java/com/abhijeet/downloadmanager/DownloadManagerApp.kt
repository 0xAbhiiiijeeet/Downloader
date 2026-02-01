package com.abhijeet.downloadmanager

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Configuration
import com.abhijeet.downloadmanager.core.di.appModule
import com.abhijeet.downloadmanager.core.di.databaseModule
import com.abhijeet.downloadmanager.core.di.networkModule
import com.abhijeet.downloadmanager.core.di.notificationModule
import com.abhijeet.downloadmanager.core.di.useCaseModule
import com.abhijeet.downloadmanager.notification.DownloadNotificationManager
import com.abhijeet.downloadmanager.worker.DownloadWorkerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class DownloadManagerApp : Application(), Configuration.Provider {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DownloadManagerApp)
            modules(databaseModule, networkModule, notificationModule, useCaseModule, appModule)
        }
        GlobalContext.get().get<DownloadNotificationManager>().createNotificationChannel()
    }

    override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
        .setWorkerFactory(DownloadWorkerFactory())
        .build()
}
