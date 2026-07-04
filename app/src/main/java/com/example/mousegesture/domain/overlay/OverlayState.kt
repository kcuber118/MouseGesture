package com.example.mousegesture.domain.overlay

/**
 * Manages the Overlay's visibility state (Visible/Hidden).
 * Domain model — no Android framework imports.
 *
 * Per ADR-0002: hiding the overlay does not stop the AccessibilityService.
 * The overlay can be shown again by opening the app and tapping "Bật overlay".
 */
class OverlayState {

    var visibility: OverlayVisibility = OverlayVisibility.VISIBLE
        private set

    /** Whether the overlay is currently visible. */
    val isVisible: Boolean get() = visibility == OverlayVisibility.VISIBLE

    /**
     * Hide the overlay. Idempotent — no-op if already hidden.
     * Per ADR-0002: triggered by nhấn-giữ Grip.
     */
    fun hide() {
        visibility = OverlayVisibility.HIDDEN
    }

    /**
     * Show the overlay. Idempotent — no-op if already visible.
     * Per ADR-0002: triggered by opening the app and tapping "Bật overlay".
     */
    fun show() {
        visibility = OverlayVisibility.VISIBLE
    }
}
