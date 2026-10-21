package com.example.cameratranslator.ui

import androidx.compose.runtime.*
import com.example.cameratranslator.core.MainViewModel

@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf("Camera") }

    when (currentScreen) {
        "Camera" -> {
            MainScreen(
                viewModel = viewModel,
                onNavigateToPicker = { currentScreen = "Picker" },
                onNavigateToSettings = { currentScreen = "Settings" }
            )
        }
        "Picker" -> {
            ImagePickerScreen(viewModel = viewModel)
        }
        "Settings" -> {
            SettingsScreen()
        }
    }
}
