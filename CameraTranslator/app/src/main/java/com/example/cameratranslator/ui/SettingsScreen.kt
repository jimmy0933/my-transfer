package com.example.cameratranslator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cameratranslator.core.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val initialTransparency by viewModel.transparency.collectAsState()
    var transparency by remember(initialTransparency) { mutableFloatStateOf(initialTransparency) }

    val isModelDownloaded by viewModel.isChineseModelDownloaded.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.saveTransparency(transparency)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("確定")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("翻譯框透明度", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = transparency,
                onValueChange = {
                    transparency = it
                    viewModel.setTempTransparency(it)
                },
                valueRange = 0f..1f
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("離線模型狀態", style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (isModelDownloaded) "繁體中文已下載" else "繁體中文未下載",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isModelDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            if (!isModelDownloaded) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.downloadChineseModel() }) {
                    Text("下載繁體中文模型")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.revertTempTransparency()
        }
    }
}
