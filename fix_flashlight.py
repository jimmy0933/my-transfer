import re

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/camera/CameraPreview.kt', 'r') as f:
    text = f.read()

replacement = """package com.example.cameratranslator.camera

import android.content.Context
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.cameratranslator.core.MainViewModel

@Composable
fun CameraPreviewScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(offset.x, offset.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        try {
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            val cameraProvider = cameraProviderFuture.get()
                            // Just tapping logic is handled inside preview touch listener
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Focus failed", e)
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                viewModel.processImage(imageProxy)
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )

                        viewModel.cameraControl = camera.cameraControl

                        // Setup zoom and tap to focus via PreviewView
                        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            override fun onScale(detector: ScaleGestureDetector): Boolean {
                                val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                                val delta = detector.scaleFactor
                                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                                return true
                            }
                        }
                        val scaleGestureDetector = ScaleGestureDetector(context, listener)
                        previewView.setOnTouchListener { _, event ->
                            scaleGestureDetector.onTouchEvent(event)
                            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                                val factory = previewView.meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point).build()
                                camera.cameraControl.startFocusAndMetering(action)
                            }
                            true
                        }

                    } catch (exc: Exception) {
                        // Handle errors
                    }
                }, ContextCompat.getMainExecutor(context))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
"""

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/camera/CameraPreview.kt', 'w') as f:
    f.write(replacement)

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/core/MainViewModel.kt', 'r') as f:
    text = f.read()

text = text.replace('class MainViewModel @Inject constructor(', 'import androidx.camera.core.CameraControl\n\n@HiltViewModel\nclass MainViewModel @Inject constructor(')
text = text.replace('var showOriginal = false', 'var showOriginal = false\n\n    var cameraControl: CameraControl? = null\n    var isFlashlightOn = false\n    fun toggleFlashlight() {\n        isFlashlightOn = !isFlashlightOn\n        cameraControl?.enableTorch(isFlashlightOn)\n    }')

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/core/MainViewModel.kt', 'w') as f:
    f.write(text)

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/ui/MainScreen.kt', 'r') as f:
    text = f.read()

text = text.replace('Text("Auto -> 繁體中文", color = MaterialTheme.colorScheme.onSurface)', 'Text("Auto -> 繁體中文", color = MaterialTheme.colorScheme.onSurface)\n            Button(onClick = { viewModel.toggleFlashlight() }) { Text("手電筒") }')

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/ui/MainScreen.kt', 'w') as f:
    f.write(text)
