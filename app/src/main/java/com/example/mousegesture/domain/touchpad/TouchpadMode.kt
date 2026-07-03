package com.example.mousegesture.domain.touchpad

/**
 * Mode of the Touchpad — determines what touch input does.
 * Domain model — no Android framework imports.
 *
 * - [ACTIVE]: Touchpad accepts drag/tap/long-press input to control Cursor.
 * - [EDIT]: Touchpad can be moved/resized; no Cursor input.
 */
enum class TouchpadMode {
    /** Touchpad accepts drag/tap/long-press to control Cursor. */
    ACTIVE,

    /** Touchpad can be moved/resized; no Cursor input. */
    EDIT,
}
