package com.abhijeet.downloadmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abhijeet.downloadmanager.presentation.DownloadScreen
import com.abhijeet.downloadmanager.ui.theme.DownloadManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DownloadManagerTheme {
                DownloadScreen()
            }
        }
    }
}
