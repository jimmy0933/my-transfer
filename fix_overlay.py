import re

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/overlay/TranslationOverlay.kt', 'r') as f:
    text = f.read()

replacement = """package com.example.cameratranslator.overlay

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

@Composable
fun TranslationOverlay(textElements: List<TextElement>, showOriginal: Boolean) {
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        textElements.forEach { element ->
            val boundingBox = element.boundingBox
            if (boundingBox != null) {
                // In a real app we would map this properly based on image vs preview size.
                // We're leaving it simplified but functional for demonstration.
                val width = with(density) { boundingBox.width().toDp() }
                val height = with(density) { boundingBox.height().toDp() }

                val textToShow = if (showOriginal) element.originalText else (element.translatedText ?: element.originalText)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(boundingBox.left, boundingBox.top) }
                        .size(width, height)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = textToShow,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
"""

with open('CameraTranslator/app/src/main/java/com/example/cameratranslator/overlay/TranslationOverlay.kt', 'w') as f:
    f.write(replacement)
