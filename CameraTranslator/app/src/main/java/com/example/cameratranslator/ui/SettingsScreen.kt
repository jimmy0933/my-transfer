package com.example.cameratranslator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cameratranslator.core.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var opacity by remember { mutableFloatStateOf(viewModel.overlayOpacity) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← 返回相機") }
        Text("翻譯設定", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text("翻譯框不透明度：" + (opacity * 100).toInt() + "%")
        Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.35f..1f)
        Spacer(Modifier.height(12.dp))
        Text("離線翻譯模型會在首次翻譯時下載。首次使用請保持網路連線。")
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { viewModel.saveOverlayOpacity(opacity); onBack() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("確定並儲存") }
    }
}
