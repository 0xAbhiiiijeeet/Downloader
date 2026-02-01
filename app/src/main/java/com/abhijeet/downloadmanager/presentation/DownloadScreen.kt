package com.abhijeet.downloadmanager.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.permission.NotificationPermissionHandler
import com.abhijeet.downloadmanager.presentation.components.DownloadItemCard
import com.abhijeet.downloadmanager.presentation.components.EmptyDownloadsView
import com.abhijeet.downloadmanager.presentation.components.UrlInputCard
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel = koinViewModel(),
    notificationPermissionHandler: NotificationPermissionHandler = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onEvent(DownloadEvent.PermissionResultReceived(granted))
    }

    LaunchedEffect(viewModel) {
        viewModel.intents.collect { intent ->
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    DownloadScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onStartDownload = {
            if (notificationPermissionHandler.shouldRequestPermission() &&
                !notificationPermissionHandler.isGranted()
            ) {
                permissionLauncher.launch(notificationPermissionHandler.permission())
            }
            viewModel.onEvent(DownloadEvent.StartDownloadClicked)
        },
    )
}

@Composable
private fun DownloadScreenContent(
    uiState: DownloadUiState,
    onEvent: (DownloadEvent) -> Unit,
    onStartDownload: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DownloadScreenHeader()

            UrlInputCard(
                url = uiState.url,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onUrlChanged = { onEvent(DownloadEvent.UrlChanged(it)) },
                onStartDownloadClicked = onStartDownload,
            )

            DownloadHistory(
                modifier = Modifier.weight(1f),
                downloads = uiState.downloads,
                onClearDownloadsClicked = { onEvent(DownloadEvent.ClearDownloadsClicked) },
                onCancelClicked = { onEvent(DownloadEvent.CancelDownloadClicked(it)) },
                onRetryClicked = { onEvent(DownloadEvent.RetryDownloadClicked(it)) },
                onDeleteClicked = { onEvent(DownloadEvent.DeleteDownloadClicked(it)) },
                onOpenClicked = { onEvent(DownloadEvent.OpenFileClicked(it)) },
                onShareClicked = { onEvent(DownloadEvent.ShareFileClicked(it)) },
            )
        }
    }
}

@Composable
private fun DownloadScreenHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Download Manager",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Paste a direct file URL to download it in the background.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DownloadHistory(
    modifier: Modifier = Modifier,
    downloads: List<DownloadItem>,
    onClearDownloadsClicked: () -> Unit,
    onCancelClicked: (Long) -> Unit,
    onRetryClicked: (Long) -> Unit,
    onDeleteClicked: (Long) -> Unit,
    onOpenClicked: (DownloadItem) -> Unit,
    onShareClicked: (DownloadItem) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Download History",
                style = MaterialTheme.typography.titleMedium,
            )

            if (downloads.isNotEmpty()) {
                FilledTonalButton(onClick = onClearDownloadsClicked) {
                    Text("Clear All")
                }
            }
        }

        if (downloads.isEmpty()) {
            EmptyDownloadsView(modifier = Modifier.fillMaxWidth())
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(
                items = downloads,
                key = { download -> download.id },
            ) { download ->
                DownloadItemCard(
                    item = download,
                    onCancelClicked = { onCancelClicked(download.id) },
                    onRetryClicked = { onRetryClicked(download.id) },
                    onDeleteClicked = { onDeleteClicked(download.id) },
                    onOpenClicked = { onOpenClicked(download) },
                    onShareClicked = { onShareClicked(download) },
                )
            }
        }
    }
}

