package com.example.mousegesture.service.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.example.mousegesture.domain.cursor.CursorController
import com.example.mousegesture.domain.touchpad.TouchpadMode
import com.example.mousegesture.domain.touchpad.TouchpadRect
import com.example.mousegesture.domain.touchpad.TouchpadState

/**
 * Root view of the accessibility overlay.
 *
 * Per ADR-0001: single full-screen overlay. This root is NOT clickable —
 * touches outside the touchpad (and grip) pass through to the app underneath.
 * The [TouchpadView] child handles cursor input in Active mode.
 * The Grip and resize handles are drawn and handled here in Edit mode.
 * Cursor is drawn via [onDraw] at [CursorController.position].
 */
class OverlayRootView(
    context: Context,
    private val cursorController: CursorController,
    private val touchpadView: TouchpadView,
    private val touchpadState: TouchpadState,
) : FrameLayout(context) {

    companion object {
        /** Grip size in pixels. */
        const val GRIP_SIZE_PX = 36f
        /** Resize handle radius in pixels. */
        const val RESIZE_HANDLE_RADIUS_PX = 10f
    }

    private val cursorPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val cursorRadius = 12f

    private val gripPaint = Paint().apply {
        color = Color.argb(200, 80, 80, 80)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val activeBorderPaint = Paint().apply {
        color = Color.argb(80, 128, 128, 128)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val editBorderPaint = Paint().apply {
        color = Color.argb(180, 255, 165, 0) // orange border in edit mode
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val resizeHandlePaint = Paint().apply {
        color = Color.argb(200, 255, 165, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Drag state for Grip
    private var isDraggingGrip = false
    private var lastGripX = 0f
    private var lastGripY = 0f

    // Drag state for resize handles
    private var isDraggingResize = false
    private var resizingCorner: ResizeCorner? = null
    private var lastResizeX = 0f
    private var lastResizeY = 0f

    init {
        // Make root not clickable — touches pass through
        isClickable = false
        isFocusable = false

        // Touchpad is initially positioned by layout logic in onLayout
        val touchpadParams = LayoutParams(0, 0)
        addView(touchpadView, touchpadParams)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layoutTouchpad()
    }

    /**
     * Position the touchpad view according to [touchpadState.rect].
     */
    fun layoutTouchpad() {
        val rect = touchpadState.rect
        touchpadView.layout(
            rect.left.toInt(),
            rect.top.toInt(),
            rect.right.toInt(),
            rect.bottom.toInt(),
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Intercept if touch lands within touchpad rect or grip
        val x = ev.x
        val y = ev.y

        if (isOnGrip(x, y)) {
            return true // Let this view handle grip interaction
        }

        if (touchpadState.mode == TouchpadMode.EDIT && isOnResizeHandle(x, y) != null) {
            return true // Let this view handle resize interaction
        }

        val touchpadRect = android.graphics.Rect()
        touchpadView.getHitRect(touchpadRect)
        return touchpadRect.contains(x.toInt(), y.toInt())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (isOnGrip(x, y)) {
                    // Grip tap/drag
                    isDraggingGrip = true
                    lastGripX = x
                    lastGripY = y
                    return true
                }

                val corner = isOnResizeHandle(x, y)
                if (touchpadState.mode == TouchpadMode.EDIT && corner != null) {
                    isDraggingResize = true
                    resizingCorner = corner
                    lastResizeX = x
                    lastResizeY = y
                    return true
                }

                // Touch within touchpad — forward to touchpad if in Active mode
                val touchpadRect = android.graphics.Rect()
                touchpadView.getHitRect(touchpadRect)
                if (touchpadRect.contains(x.toInt(), y.toInt())) {
                    if (touchpadState.mode == TouchpadMode.ACTIVE) {
                        val offsetEvent = MotionEvent.obtain(
                            event.downTime,
                            event.eventTime,
                            event.action,
                            x - touchpadRect.left,
                            y - touchpadRect.top,
                            event.metaState,
                        )
                        touchpadView.onTouchEvent(offsetEvent)
                        offsetEvent.recycle()
                    }
                    // In Edit mode, consume but do nothing (no cursor input)
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDraggingGrip && touchpadState.mode == TouchpadMode.EDIT) {
                    val dx = x - lastGripX
                    val dy = y - lastGripY
                    touchpadState.moveBy(dx, dy)
                    lastGripX = x
                    lastGripY = y
                    layoutTouchpad()
                    invalidate()
                    return true
                }
                if (isDraggingResize && touchpadState.mode == TouchpadMode.EDIT) {
                    val dx = x - lastResizeX
                    val dy = y - lastResizeY
                    handleResize(dx, dy)
                    lastResizeX = x
                    lastResizeY = y
                    layoutTouchpad()
                    invalidate()
                    return true
                }
                // Forward drag to touchpad in Active mode
                val touchpadRect = android.graphics.Rect()
                touchpadView.getHitRect(touchpadRect)
                if (touchpadRect.contains(x.toInt(), y.toInt()) &&
                    touchpadState.mode == TouchpadMode.ACTIVE
                ) {
                    val offsetEvent = MotionEvent.obtain(
                        event.downTime,
                        event.eventTime,
                        event.action,
                        x - touchpadRect.left,
                        y - touchpadRect.top,
                        event.metaState,
                    )
                    touchpadView.onTouchEvent(offsetEvent)
                    offsetEvent.recycle()
                    return true
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isDraggingGrip) {
                    if (!hasMovedSignificantly()) {
                        // Tap on Grip = toggle mode
                        touchpadState.toggleMode()
                        // Enable/disable touchpad input based on mode
                        touchpadView.isEnabled = touchpadState.mode == TouchpadMode.ACTIVE
                        layoutTouchpad()
                        invalidate()
                    }
                    isDraggingGrip = false
                    return true
                }
                if (isDraggingResize) {
                    isDraggingResize = false
                    resizingCorner = null
                    return true
                }
                // Forward up to touchpad in Active mode
                val touchpadRect = android.graphics.Rect()
                touchpadView.getHitRect(touchpadRect)
                if (touchpadRect.contains(x.toInt(), y.toInt()) &&
                    touchpadState.mode == TouchpadMode.ACTIVE
                ) {
                    val offsetEvent = MotionEvent.obtain(
                        event.downTime,
                        event.eventTime,
                        event.action,
                        x - touchpadRect.left,
                        y - touchpadRect.top,
                        event.metaState,
                    )
                    touchpadView.onTouchEvent(offsetEvent)
                    offsetEvent.recycle()
                    return true
                }
                return true
            }
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw cursor
        val pos = cursorController.position
        canvas.drawCircle(pos.x, pos.y, cursorRadius, cursorPaint)

        // Draw touchpad border (visual distinction between modes)
        val rect = touchpadState.rect
        val borderPaint = if (touchpadState.mode == TouchpadMode.EDIT) editBorderPaint else activeBorderPaint
        canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, borderPaint)

        // Draw Grip (always visible, top-left corner of touchpad)
        drawGrip(canvas, rect)

        // Draw resize handles in Edit mode
        if (touchpadState.mode == TouchpadMode.EDIT) {
            drawResizeHandles(canvas, rect)
        }
    }

    private fun drawGrip(canvas: Canvas, rect: TouchpadRect) {
        val gripLeft = rect.left
        val gripTop = rect.top
        canvas.drawRect(
            gripLeft,
            gripTop,
            gripLeft + GRIP_SIZE_PX,
            gripTop + GRIP_SIZE_PX,
            gripPaint,
        )
    }

    private fun drawResizeHandles(canvas: Canvas, rect: TouchpadRect) {
        // Four corner handles
        val corners = listOf(
            rect.right to rect.top,     // top-right
            rect.right to rect.bottom,  // bottom-right
            rect.left to rect.bottom,   // bottom-left
        )
        for ((cx, cy) in corners) {
            canvas.drawCircle(cx, cy, RESIZE_HANDLE_RADIUS_PX, resizeHandlePaint)
        }
    }

    /**
     * Check if touch coordinates are on the Grip.
     */
    private fun isOnGrip(x: Float, y: Float): Boolean {
        val rect = touchpadState.rect
        return x >= rect.left && x <= rect.left + GRIP_SIZE_PX &&
               y >= rect.top && y <= rect.top + GRIP_SIZE_PX
    }

    /**
     * Check if touch coordinates are on a resize handle.
     * Returns which corner, or null if not on any handle.
     */
    private fun isOnResizeHandle(x: Float, y: Float): ResizeCorner? {
        if (touchpadState.mode != TouchpadMode.EDIT) return null
        val rect = touchpadState.rect
        val hitRadius = RESIZE_HANDLE_RADIUS_PX * 2 // generous hit area

        if (distance(x, y, rect.right, rect.top) <= hitRadius) return ResizeCorner.TOP_RIGHT
        if (distance(x, y, rect.right, rect.bottom) <= hitRadius) return ResizeCorner.BOTTOM_RIGHT
        if (distance(x, y, rect.left, rect.bottom) <= hitRadius) return ResizeCorner.BOTTOM_LEFT
        return null
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()
    }

    private fun hasMovedSignificantly(): Boolean {
        // If drag distance is small, it was a tap
        val dx = lastGripX - (if (isDraggingGrip) lastGripX else lastGripX)
        return false // Simplified: for now, any DOWN+UP on Grip is a tap
        // TODO: Track initial down position for better tap vs drag detection
    }

    private fun handleResize(dx: Float, dy: Float) {
        when (resizingCorner) {
            ResizeCorner.TOP_RIGHT -> touchpadState.resizeBy(dx, -dy)
            ResizeCorner.BOTTOM_RIGHT -> touchpadState.resizeBy(dx, dy)
            ResizeCorner.BOTTOM_LEFT -> touchpadState.resizeBy(-dx, dy)
            null -> { /* no-op */ }
        }
    }

    /**
     * Call after cursor position changes to trigger redraw.
     */
    fun updateCursor() {
        invalidate()
    }

    /**
     * Which corner of the touchpad is being used for resize.
     */
    enum class ResizeCorner {
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
    }
}
