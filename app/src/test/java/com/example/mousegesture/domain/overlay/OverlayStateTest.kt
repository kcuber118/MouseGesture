package com.example.mousegesture.domain.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayStateTest {

    // --- Tracer bullet: initial state is VISIBLE ---

    @Test
    fun initialState_isVisible() {
        val state = OverlayState()
        assertEquals(OverlayVisibility.VISIBLE, state.visibility)
    }

    // --- hide() transitions VISIBLE → HIDDEN ---

    @Test
    fun hide_fromVisible_transitionsToHidden() {
        val state = OverlayState()
        state.hide()
        assertEquals(OverlayVisibility.HIDDEN, state.visibility)
    }

    // --- show() transitions HIDDEN → VISIBLE ---

    @Test
    fun show_fromHidden_transitionsToVisible() {
        val state = OverlayState()
        state.hide()
        state.show()
        assertEquals(OverlayVisibility.VISIBLE, state.visibility)
    }

    // --- hide() is idempotent ---

    @Test
    fun hide_fromHidden_staysHidden() {
        val state = OverlayState()
        state.hide()
        state.hide()
        assertEquals(OverlayVisibility.HIDDEN, state.visibility)
    }

    // --- show() is idempotent ---

    @Test
    fun show_fromVisible_staysVisible() {
        val state = OverlayState()
        state.show()
        assertEquals(OverlayVisibility.VISIBLE, state.visibility)
    }

    // --- isVisible convenience property ---

    @Test
    fun isVisible_returnsTrueWhenVisible() {
        val state = OverlayState()
        assertEquals(true, state.isVisible)
    }

    @Test
    fun isVisible_returnsFalseWhenHidden() {
        val state = OverlayState()
        state.hide()
        assertEquals(false, state.isVisible)
    }
}
