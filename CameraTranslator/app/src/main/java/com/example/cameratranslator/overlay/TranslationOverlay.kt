package com.example.cameratranslator.overlay

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cameratranslator.core.models.TextElement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import com.example.cameratranslator.core.MainViewModel

@Composable
fun TranslationOverlay(viewModel: MainViewModel, textElements: List<TextElement>, showOriginal: Boolean) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val transparency by viewModel.currentTransparency.collectAsState()

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize().onSizeChanged { containerSize = it }) {
        if (containerSize.width == 0 || containerSize.height == 0) return@Box

        val imageW = viewModel.imageWidth.toFloat()
        val imageH = viewModel.imageHeight.toFloat()

        // Calculate scale keeping aspect ratio and center crop usually used by PreviewView
        val scaleX = containerSize.width.toFloat() / imageW
        val scaleY = containerSize.height.toFloat() / imageH
        val scale = maxOf(scaleX, scaleY)

        val scaledW = imageW * scale
        val scaledH = imageH * scale

        val offsetX = (containerSize.width - scaledW) / 2f
        val offsetY = (containerSize.height - scaledH) / 2f

        textElements.forEach { element ->
            val boundingBox = element.boundingBox
            if (boundingBox != null) {
                var isTapped by remember(element.id) { mutableStateOf(false) }
                val textToShow = if (showOriginal || isTapped) element.originalText else (element.translatedText ?: element.originalText)

                val left = (boundingBox.left * scale) + offsetX
                val top = (boundingBox.top * scale) + offsetY

                // Adjust if out of bounds
                val safeLeft = left.coerceIn(0f, containerSize.width.toFloat() - 100f)
                val safeTop = top.coerceIn(0f, containerSize.height.toFloat() - 50f)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(safeLeft.toInt(), safeTop.toInt()) }
                        .wrapContentSize()
                        .background(Color.Black.copy(alpha = transparency))
                        .clickable { isTapped = !isTapped }
                        .padding(4.dp)
                ) {
                    Text(
                        text = textToShow,
                        color = Color.White,
                        fontSize = 20.sp, // Default 20sp, will wrap automatically
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}
