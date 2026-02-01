package com.abhijeet.downloadmanager.core.di


import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import com.abhijeet.downloadmanager.core.file.FileManager
import com.abhijeet.downloadmanager.core.file.FileNameResolver
import com.abhijeet.downloadmanager.data.downloader.FileDownloader
import com.abhijeet.downloadmanager.data.downloader.OkHttpFileDownloader
import com.abhijeet.downloadmanager.data.local.DownloadDatabase
import com.abhijeet.downloadmanager.data.repository.DownloadRepositoryImpl
import com.abhijeet.downloadmanager.domain.repository.DownloadRepository
import com.abhijeet.downloadmanager.domain.usecase.CancelDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.ClearDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.DeleteDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.GetDownloadByIdUseCase
import com.abhijeet.downloadmanager.domain.usecase.GetDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.OpenDownloadedFileUseCase
import com.abhijeet.downloadmanager.domain.usecase.RetryDownloadUseCase
import com.abhijeet.downloadmanager.domain.usecase.ShareDownloadedFileUseCase
import com.abhijeet.downloadmanager.domain.usecase.StartDownloadUseCase
import com.abhijeet.downloadmanager.notification.DownloadNotificationManager
import com.abhijeet.downloadmanager.permission.NotificationPermissionHandler
import com.abhijeet.downloadmanager.presentation.DownloadViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { WorkManager.getInstance(androidContext()) }
    single { FileNameResolver() }
    single { FileManager(androidContext()) }
    single { NotificationPermissionHandler(androidContext()) }
    single<DownloadRepository> { DownloadRepositoryImpl(get(), get(), get(), get()) }
    viewModel { DownloadViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            DownloadDatabase::class.java,
            "download.db"
        ).addMigrations(migration1To2).build()
    }

    single { get<DownloadDatabase>().downloadDao() }
}


val networkModule = module {
    single { OkHttpClient() }
    single<FileDownloader> { OkHttpFileDownloader(get()) }
}

val notificationModule = module {
    single { DownloadNotificationManager(androidContext()) }
}


val useCaseModule = module {
    factory { StartDownloadUseCase(get()) }
    factory { GetDownloadUseCase(get()) }
    factory { GetDownloadByIdUseCase(get()) }
    factory { DeleteDownloadUseCase(get()) }
    factory { ClearDownloadUseCase(get()) }
    factory { CancelDownloadUseCase(get()) }
    factory { RetryDownloadUseCase(get()) }
    factory { OpenDownloadedFileUseCase(get(), androidContext().packageManager) }
    factory { ShareDownloadedFileUseCase(get()) }
}

private val migration1To2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE downloads ADD COLUMN mimeType TEXT")
        db.execSQL("ALTER TABLE downloads ADD COLUMN downloadedBytes INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE downloads ADD COLUMN totalBytes INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE downloads ADD COLUMN completedAt INTEGER")
        db.execSQL("ALTER  TABLE downloads ADD COLUMN errorMessage TEXT")
        db.execSQL("ALTER TABLE downloads ADD COLUMN workId TEXT")
    }
}


