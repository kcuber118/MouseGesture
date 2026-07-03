package com.example.mousegesture.domain.touchpad

import com.example.mousegesture.domain.model.ScreenBounds

/**
 * Manages the Touchpad's mode (Active/Edit) and position/size on screen.
 * Domain logic — no Android framework imports.
 *
 * In [TouchpadMode.ACTIVE], only mode toggle is allowed (no move/resize).
 * In [TouchpadMode.EDIT], the touchpad can be moved and resized within screen bounds,
 * respecting min/max size constraints.
 */
class TouchpadState(
    private val screenBounds: ScreenBounds,
) {

    companion object {
        /** Default touchpad size in pixels (200dp at density 1). */
        const val DEFAULT_SIZE_PX = 200f

        /** Minimum allowed touchpad dimension in pixels. */
        const val MIN_SIZE_PX = 100f

        /** Maximum allowed touchpad dimension in pixels. */
        const val MAX_SIZE_PX = 600f
    }

    var mode: TouchpadMode = TouchpadMode.ACTIVE
        private set

    var rect: TouchpadRect = defaultRect()
        private set

    /**
     * Toggle between Active and Edit mode.
     */
    fun toggleMode() {
        mode = when (mode) {
            TouchpadMode.ACTIVE -> TouchpadMode.EDIT
            TouchpadMode.EDIT -> TouchpadMode.ACTIVE
        }
    }

    /**
     * Move the touchpad by a relative delta.
     * Only effective in [TouchpadMode.EDIT]; no-op in [TouchpadMode.ACTIVE].
     * The rect is clamped to screen bounds after moving.
     */
    fun moveBy(deltaX: Float, deltaY: Float) {
        if (mode != TouchpadMode.EDIT) return

        val newLeft = rect.left + deltaX
        val newTop = rect.top + deltaY
        val newRight = rect.right + deltaX
        val newBottom = rect.bottom + deltaY

        rect = clampToScreen(newLeft, newTop, newRight, newBottom)
    }

    /**
     * Resize the touchpad by a relative delta.
     * Only effective in [TouchpadMode.EDIT]; no-op in [TouchpadMode.ACTIVE].
     * Anchor is top-left corner: left/top stay fixed, right/bottom expand/shrink.
     * Size is clamped to [MIN_SIZE_PX]..[MAX_SIZE_PX], then rect is clamped to screen bounds.
     */
    fun resizeBy(deltaWidth: Float, deltaHeight: Float) {
        if (mode != TouchpadMode.EDIT) return

        val desiredWidth = (rect.width + deltaWidth).coerceIn(MIN_SIZE_PX, MAX_SIZE_PX)
        val desiredHeight = (rect.height + deltaHeight).coerceIn(MIN_SIZE_PX, MAX_SIZE_PX)

        // Clamp right/bottom to screen bounds; anchor top-left
        val newRight = (rect.left + desiredWidth).coerceAtMost(screenBounds.width)
        val newBottom = (rect.top + desiredHeight).coerceAtMost(screenBounds.height)

        rect = TouchpadRect(
            left = rect.left,
            top = rect.top,
            right = newRight,
            bottom = newBottom,
        )
    }

    /**
     * Clamp a raw rect so that it stays within screen bounds, preserving its size.
     * Priority: shift left/up if overflowing right/bottom, then clamp left/top to 0.
     */
    private fun clampToScreen(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): TouchpadRect {
        val w = right - left
        val h = bottom - top

        // If width > screen width, just clamp to screen (shouldn't happen with valid sizes)
        val clampedRight = minOf(right, screenBounds.width)
        val clampedLeft = if (clampedRight - w < 0f) 0f else clampedRight - w

        val clampedBottom = minOf(bottom, screenBounds.height)
        val clampedTop = if (clampedBottom - h < 0f) 0f else clampedBottom - h

        return TouchpadRect(
            left = clampedLeft,
            top = clampedTop,
            right = clampedLeft + w,
            bottom = clampedTop + h,
        )
    }

    private fun defaultRect(): TouchpadRect {
        val left = screenBounds.width - DEFAULT_SIZE_PX
        val top = screenBounds.height - DEFAULT_SIZE_PX
        return TouchpadRect(
            left = left,
            top = top,
            right = screenBounds.width,
            bottom = screenBounds.height,
        )
    }
}
