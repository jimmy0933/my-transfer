package com.example.cameratranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cameratranslator.core.MainViewModel
import com.example.cameratranslator.ui.MainScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

                    if (cameraPermissionState.status.isGranted) {
                        com.example.cameratranslator.ui.MainAppNavigation(viewModel)
                    } else {
                        androidx.compose.material3.Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            androidx.compose.material3.Text("請求相機權限")
                        }
                    }
                }
            }
        }
    }
}
