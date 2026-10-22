package com.example.cameratranslator.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cameratranslator.core.models.TextElement
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun TranslationOverlay(
    textElements: List<TextElement>,
    showOriginal: Boolean,
    opacity: Float = 0.88f
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier) {
        val screenWidth = with(density) { maxWidth.toPx() }
        val screenHeight = with(density) { maxHeight.toPx() }
        textElements.forEach { element ->
            val box = element.boundingBox ?: return@forEach
            // Do not draw placeholders when the translation model is unavailable.
            val text = if (showOriginal) element.originalText else element.translatedText
            if (text.isNullOrBlank() || element.imageWidth <= 0 || element.imageHeight <= 0) return@forEach

            // PreviewView uses FILL_CENTER. Apply the same center-crop transform to
            // OCR boxes in the rotated ImageAnalysis coordinate space.
            val factor = max(screenWidth / element.imageWidth, screenHeight / element.imageHeight)
            val extraX = (screenWidth - element.imageWidth * factor) / 2f
            val extraY = (screenHeight - element.imageHeight * factor) / 2f
            val rawX = extraX + box.left * factor
            val rawY = extraY + box.top * factor
            val pxX = rawX.coerceIn(0f, (screenWidth - with(density) { 110.dp.toPx() }).coerceAtLeast(0f))
            val pxY = rawY.coerceIn(0f, (screenHeight - with(density) { 42.dp.toPx() }).coerceAtLeast(0f))
            val minWidthPx = with(density) { 136.dp.toPx() }
            val desiredWidth = max(minWidthPx, box.width() * factor + with(density) { 24.dp.toPx() })
            val availableWidth = (screenWidth - pxX - with(density) { 8.dp.toPx() }).coerceAtLeast(minWidthPx)
            val actualWidth = minOf(desiredWidth.coerceAtMost(with(density) { 300.dp.toPx() }), availableWidth)
            Box(
                modifier = Modifier
                    .offset { IntOffset(pxX.roundToInt(), pxY.roundToInt()) }
                    .width(with(density) { actualWidth.toDp() })
                    .heightIn(min = 32.dp)
                    .background(Color(0xFF102629).copy(alpha = opacity), RoundedCornerShape(8.dp))
                    .padding(PaddingValues(horizontal = 9.dp, vertical = 6.dp))
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
