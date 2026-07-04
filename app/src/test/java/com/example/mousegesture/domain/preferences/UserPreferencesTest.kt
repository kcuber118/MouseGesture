package com.example.mousegesture.domain.preferences

import com.example.mousegesture.domain.cursor.AccelerationCurve
import com.example.mousegesture.domain.model.Point
import com.example.mousegesture.domain.model.ScreenBounds
import com.example.mousegesture.domain.touchpad.TouchpadRect
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {

    private val screenBounds = ScreenBounds(width = 1080f, height = 2400f)

    // --- Tracer bullet: UserPreferences data class round-trip ---

    @Test
    fun defaultValues_areConsistent() {
        val prefs = UserPreferences()

        assertEquals(AccelerationCurve.DEFAULT_SENSITIVITY, prefs.sensitivity, 0.001f)
        assertEquals(null, prefs.touchpadRect)
        assertEquals(null, prefs.cursorPosition)
    }

    @Test
    fun withSensitivity_createsNewInstanceWithUpdatedSensitivity() {
        val prefs = UserPreferences(sensitivity = 1.0f)
        val updated = prefs.withSensitivity(2.5f)

        assertEquals(1.0f, prefs.sensitivity, 0.001f) // original unchanged
        assertEquals(2.5f, updated.sensitivity, 0.001f) // new has updated value
    }

    @Test
    fun withTouchpadRect_createsNewInstanceWithRect() {
        val rect = TouchpadRect(left = 100f, top = 200f, right = 300f, bottom = 400f)
        val prefs = UserPreferences()
        val updated = prefs.withTouchpadRect(rect)

        assertEquals(null, prefs.touchpadRect) // original unchanged
        assertEquals(rect, updated.touchpadRect)
    }

    @Test
    fun withCursorPosition_createsNewInstanceWithPosition() {
        val pos = Point(540f, 1200f)
        val prefs = UserPreferences()
        val updated = prefs.withCursorPosition(pos)

        assertEquals(null, prefs.cursorPosition) // original unchanged
        assertEquals(pos, updated.cursorPosition)
    }

    @Test
    fun fullRoundTrip_allFieldsPreserved() {
        val rect = TouchpadRect(left = 100f, top = 200f, right = 300f, bottom = 400f)
        val pos = Point(540f, 1200f)
        val prefs = UserPreferences(
            sensitivity = 1.8f,
            touchpadRect = rect,
            cursorPosition = pos,
        )

        assertEquals(1.8f, prefs.sensitivity, 0.001f)
        assertEquals(rect, prefs.touchpadRect)
        assertEquals(pos, prefs.cursorPosition)
    }
}
