package com.abhijeet.downloadmanager.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhijeet.downloadmanager.domain.model.DownloadItem
import com.abhijeet.downloadmanager.domain.model.DownloadStatus
import java.text.DateFormat
import java.util.Date
import kotlin.compareTo
import kotlin.div
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun DownloadItemCard(
    item: DownloadItem,
    onCancelClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onOpenClicked: () -> Unit,
    onShareClicked: () -> Unit,
) {
    val createdAtLabel = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
    ).format(Date(item.createdAt))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = item.fileName,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = item.url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Status: ${item.status.name}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Progress: ${item.progress}%",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            LinearProgressIndicator(
                progress = {
                    when {
                        item.status == DownloadStatus.Completed -> 1f
                        item.totalBytes > 0L -> item.progress / 100f
                        else -> 0f
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = progressLabel(item),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!item.errorMessage.isNullOrBlank()) {
                Text(
                    text = item.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = "Created: $createdAtLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (item.status) {
                    DownloadStatus.Queued,
                    DownloadStatus.Running,
                        -> {
                        OutlinedButton(onClick = onCancelClicked) {
                            Text("Cancel")
                        }
                    }

                    DownloadStatus.Failed,
                    DownloadStatus.Cancelled,
                        -> {
                        OutlinedButton(onClick = onRetryClicked) {
                            Text("Retry")
                        }
                    }

                    DownloadStatus.Completed -> {
                        OutlinedButton(onClick = onOpenClicked) {
                            Text("Open")
                        }
                        OutlinedButton(onClick = onShareClicked) {
                            Text("Share")
                        }
                    }
                }

                OutlinedButton(onClick = onDeleteClicked) {
                    Text("Delete")
                }
            }
        }
    }
}

private fun progressLabel(item: DownloadItem): String {
    val downloaded = formatBytes(item.downloadedBytes)
    val total = formatBytes(item.totalBytes)

    return if (item.totalBytes > 0) {
        "$downloaded / $total"
    } else if (item.status == DownloadStatus.Running) {
        "$downloaded downloaded"
    } else {
        "Queued for background download"
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceAtMost(units.lastIndex)
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    return String.format("%.1f %s", value, units[digitGroups])
}
