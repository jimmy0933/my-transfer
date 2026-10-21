package com.example.cameratranslator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var transparency by remember { mutableFloatStateOf(0.5f) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("設定", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("翻譯框透明度")
        Slider(
            value = transparency,
            onValueChange = { transparency = it },
            valueRange = 0f..1f
        )

        // Settings could include offline models download status
        Text("離線模型狀態: 繁體中文已下載 (Demo)")
    }
}
