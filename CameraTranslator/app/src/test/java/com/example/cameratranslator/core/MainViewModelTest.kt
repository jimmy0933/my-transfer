package com.example.cameratranslator.core

import org.junit.Test
import org.junit.Assert.*

class MainViewModelTest {

    // Simulate Rect for unit tests since android.graphics.Rect is stubbed out
    data class SimpleRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        fun intersect(other: SimpleRect): Boolean {
            return this.left < other.right && other.left < this.right && this.top < other.bottom && other.top < this.bottom
        }

        fun width() = right - left
        fun height() = bottom - top

        fun intersection(other: SimpleRect): SimpleRect {
            val iLeft = maxOf(this.left, other.left)
            val iTop = maxOf(this.top, other.top)
            val iRight = minOf(this.right, other.right)
            val iBottom = minOf(this.bottom, other.bottom)
            return SimpleRect(iLeft, iTop, iRight, iBottom)
        }
    }

    @Test
    fun testIoULogicSimulated() {
        val rect1 = SimpleRect(0, 0, 100, 100)
        val rect2 = SimpleRect(50, 50, 150, 150)

        val didIntersect = rect1.intersect(rect2)
        assertTrue(didIntersect)

        val intersectRect = rect1.intersection(rect2)
        val intersectArea = intersectRect.width() * intersectRect.height()
        assertEquals(50 * 50, intersectArea)

        val unionArea = (rect1.width() * rect1.height()) + (rect2.width() * rect2.height()) - intersectArea
        assertEquals(10000 + 10000 - 2500, unionArea)

        val iou = intersectArea.toDouble() / unionArea.toDouble()
        assertEquals(2500.0 / 17500.0, iou, 0.001)
    }

    @Test
    fun testTranslationCacheSimulated() {
        val cache = mutableMapOf<String, String>()
        val originalText = "Hello"

        cache[originalText] = "你好"

        val cachedText = cache[originalText]
        assertEquals("你好", cachedText)
    }
}
