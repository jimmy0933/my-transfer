package com.example.cameratranslator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cameratranslator.camera.CameraPreviewScreen
import com.example.cameratranslator.core.MainViewModel
import com.example.cameratranslator.overlay.TranslationOverlay

@Composable
fun MainScreen(viewModel: MainViewModel, onNavigateToPicker: () -> Unit, onNavigateToSettings: () -> Unit) {
    val textElements by viewModel.textElements.collectAsState()
    val status by viewModel.statusMessage.collectAsState()
    var paused by remember { mutableStateOf(false) }
    var showOriginal by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        CameraPreviewScreen(viewModel)
        Box(Modifier.fillMaxSize()) {
            TranslationOverlay(textElements, showOriginal, viewModel.overlayOpacity)
        }
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("自動 → 繁中", color = Color.White, modifier = Modifier
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                .padding(8.dp))
            Spacer(Modifier.weight(1f))
            Button(onClick = viewModel::toggleFlashlight, contentPadding = PaddingValues(8.dp)) { Text("手電筒") }
            Button(onClick = onNavigateToSettings, contentPadding = PaddingValues(8.dp)) { Text("設定") }
        }
        if (status != null && !paused) {
            Text(
                status ?: "",
                color = Color.White,
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = 76.dp, start = 18.dp, end = 18.dp)
                    .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            )
        }
        Row(
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onNavigateToPicker) { Text("相簿") }
            Button(onClick = {
                paused = !paused
                viewModel.isPaused = paused
            }) { Text(if (paused) "恢復" else "暫停") }
            Button(onClick = {
                showOriginal = !showOriginal
                viewModel.showOriginal = showOriginal
            }) { Text(if (showOriginal) "翻譯" else "原文") }
        }
    }
}
