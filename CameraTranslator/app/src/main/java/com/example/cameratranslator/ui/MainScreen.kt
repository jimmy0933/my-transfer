package com.example.cameratranslator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cameratranslator.camera.CameraPreviewScreen
import com.example.cameratranslator.core.MainViewModel
import com.example.cameratranslator.overlay.TranslationOverlay

@Composable
fun MainScreen(viewModel: MainViewModel, onNavigateToPicker: () -> Unit, onNavigateToSettings: () -> Unit) {
    val textElements by viewModel.textElements.collectAsState()

    var isPaused by remember { mutableStateOf(false) }
    var showOriginal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreviewScreen(viewModel)
        TranslationOverlay(textElements, showOriginal)

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Auto -> 繁體中文", color = MaterialTheme.colorScheme.onSurface)
            Button(onClick = { viewModel.toggleFlashlight() }) { Text("手電筒") }
            Button(onClick = onNavigateToSettings) { Text("設定") }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onNavigateToPicker) { Text("相簿") }
            Button(onClick = {
                isPaused = !isPaused
                viewModel.isPaused = isPaused
            }) {
                Text(if (isPaused) "恢復" else "暫停")
            }
            Button(onClick = {
                showOriginal = !showOriginal
                viewModel.showOriginal = showOriginal
            }) {
                Text(if (showOriginal) "翻譯" else "原文")
            }
        }
    }
}
