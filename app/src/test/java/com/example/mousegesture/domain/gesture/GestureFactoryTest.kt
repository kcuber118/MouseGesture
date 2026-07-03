package com.example.mousegesture.domain.gesture

import com.example.mousegesture.domain.model.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class GestureFactoryTest {

    private val factory = GestureFactory()

    @Test
    fun createTapAt_returnsGestureWithCursorCoordinates() {
        val point = Point(500f, 800f)
        val gesture = factory.createTapAt(point)
        assertEquals(500f, gesture.x, 0.001f)
        assertEquals(800f, gesture.y, 0.001f)
    }

    @Test
    fun createTapAt_returnsDefaultDuration() {
        val point = Point(100f, 200f)
        val gesture = factory.createTapAt(point)
        assertEquals(GestureFactory.DEFAULT_TAP_DURATION_MS, gesture.durationMs)
    }

    @Test
    fun createLongPressAt_returnsGestureWithCursorCoordinates() {
        val point = Point(300f, 600f)
        val gesture = factory.createLongPressAt(point)
        assertEquals(300f, gesture.x, 0.001f)
        assertEquals(600f, gesture.y, 0.001f)
    }

    @Test
    fun createLongPressAt_returnsDefaultDuration() {
        val point = Point(100f, 200f)
        val gesture = factory.createLongPressAt(point)
        assertEquals(GestureFactory.DEFAULT_LONG_PRESS_DURATION_MS, gesture.durationMs)
    }
}
