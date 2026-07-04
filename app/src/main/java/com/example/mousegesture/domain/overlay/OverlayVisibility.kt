package com.example.mousegesture.domain.overlay

/**
 * Visibility state of the Overlay.
 * Domain model — no Android framework imports.
 *
 * - [VISIBLE]: Overlay is rendered and accepts touch input on the Touchpad.
 * - [HIDDEN]: Overlay is not rendered and does not accept any input.
 *   The AccessibilityService remains running; the overlay can be shown again.
 */
enum class OverlayVisibility {
    /** Overlay is rendered and accepts touch input on the Touchpad. */
    VISIBLE,

    /** Overlay is not rendered and does not accept any input. */
    HIDDEN,
}
