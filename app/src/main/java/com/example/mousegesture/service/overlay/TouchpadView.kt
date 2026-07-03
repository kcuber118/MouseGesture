package com.example.mousegesture.service.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.example.mousegesture.domain.gesture.GestureType
import com.example.mousegesture.domain.gesture.TouchGestureClassifier

/**
 * A fixed-size touchpad view that captures touch events (drag, tap, long-press).
 * Positioned at bottom-right corner of the screen.
 *
 * Uses [TouchGestureClassifier] to disambiguate tap / long-press / move
 * based on duration and distance thresholds.
 */
class TouchpadView(context: Context) : View(context) {

    var onDragCallback: ((deltaX: Float, deltaY: Float, speed: Float) -> Unit)? = null
    var onTapCallback: (() -> Unit)? = null
    var onLongPressCallback: (() -> Unit)? = null

    private val backgroundPaint = Paint().apply {
        color = Color.argb(80, 128, 128, 128) // semi-transparent gray
        style = Paint.Style.FILL
    }

    private val classifier = TouchGestureClassifier()

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastMoveTime = 0L
    private var downTime = 0L

    // When gesture is classified as MOVE, we track drag deltas
    private var isMoving = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                downTime = event.downTime
                isMoving = false
                classifier.onDown()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                val elapsed = (event.eventTime - lastMoveTime).coerceAtLeast(1L)
                val moveDistance = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                val speed = moveDistance / elapsed.toFloat() // px/ms
                val gestureType = classifier.onMove(dx, dy)

                if (gestureType == GestureType.MOVE) {
                    isMoving = true
                    onDragCallback?.invoke(dx, dy, speed)
                }
                lastTouchX = event.x
                lastTouchY = event.y
                lastMoveTime = event.eventTime
                return true
            }
            MotionEvent.ACTION_UP -> {
                val elapsedMs = event.eventTime - downTime
                val gestureType = classifier.onUp(elapsedMs)

                when (gestureType) {
                    GestureType.TAP -> onTapCallback?.invoke()
                    GestureType.LONG_PRESS -> onLongPressCallback?.invoke()
                    GestureType.MOVE -> { /* No injection on release after move */ }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
