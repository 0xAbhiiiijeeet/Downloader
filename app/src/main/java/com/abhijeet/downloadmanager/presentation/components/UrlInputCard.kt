package com.abhijeet.downloadmanager.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun UrlInputCard(
    url: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUrlChanged: (String) -> Unit,
    onStartDownloadClicked: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "File URL",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Only direct file links are supported. Pages from video platforms are rejected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Direct file URL") },
                placeholder = { Text("https://example.com/sample.pdf") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                isError = errorMessage != null,
                supportingText = {
                    if (errorMessage != null) {
                        Text(text = errorMessage)
                    }
                },
                singleLine = true,
            )

            Button(
                onClick = onStartDownloadClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            ) {
                Text(if (isLoading) "Working..." else "Start Download")
            }
        }
    }
}
