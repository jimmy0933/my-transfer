package com.example.cameratranslator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cameratranslator.camera.CameraPreviewScreen
import com.example.cameratranslator.core.MainViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info

import com.example.cameratranslator.overlay.TranslationOverlay

@Composable
fun MainScreen(viewModel: MainViewModel, onNavigateToPicker: () -> Unit, onNavigateToSettings: () -> Unit) {
    val textElements by viewModel.textElements.collectAsState()

    var isPaused by remember { mutableStateOf(false) }
    var showOriginal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreviewScreen(viewModel)
        TranslationOverlay(viewModel, textElements, showOriginal)

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = CircleShape
            ) {
                Text(
                    text = "自動偵測 → 繁體中文",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFloatingActionButton(
                    onClick = { viewModel.toggleFlashlight() },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text("🔦")
                }
                SmallFloatingActionButton(
                    onClick = onNavigateToSettings,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "設定")
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(onClick = onNavigateToPicker) { Text("相簿") }
            FloatingActionButton(
                onClick = {
                    isPaused = !isPaused
                    viewModel.isPaused = isPaused
                }
            ) {
                Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Search, contentDescription = if (isPaused) "恢復" else "暫停")
            }
            FilledTonalButton(onClick = {
                showOriginal = !showOriginal
                viewModel.showOriginal = showOriginal
            }) {
                Text(if (showOriginal) "翻譯" else "原文")
            }
        }
    }
}
