package com.example.mousegesture.domain.gesture

/**
 * Classification of a single-finger touch gesture on the Touchpad.
 * Domain model — no Android framework imports.
 */
enum class GestureType {
    /** Down-up nhanh + ít dịch chuyển → Inject tap tại Cursor. */
    TAP,

    /** Giữ quá ngưỡng thời gian + ít dịch chuyển → Inject long-press tại Cursor. */
    LONG_PRESS,

    /** Dịch chuyển quá ngưỡng khoảng cách → move Cursor, KHÔNG Inject khi nhả. */
    MOVE,
}
