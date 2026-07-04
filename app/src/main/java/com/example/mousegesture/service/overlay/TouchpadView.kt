package com.example.mousegesture.service.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
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
 *
 * Long-press is detected early via [Handler.postDelayed] — when the finger
 * is held beyond [TouchGestureClassifier.LONG_PRESS_DURATION_THRESHOLD_MS],
 * the [onLongPressCallback] fires immediately (while finger is still down),
 * matching Android's native long-press behavior.
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
    private val handler = Handler(Looper.getMainLooper())

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastMoveTime = 0L
    private var downTime = 0L

    // When gesture is classified as MOVE, we track drag deltas
    private var isMoving = false

    // True when long-press has already been injected via onElapsedTime
    private var longPressInjected = false

    /**
     * Runnable that checks if the finger has been held long enough for a long-press.
     * Scheduled on ACTION_DOWN, removed on ACTION_UP or when MOVE is detected.
     */
    private val longPressCheckRunnable = Runnable {
        if (!isMoving && !longPressInjected) {
            val elapsedMs = System.currentTimeMillis() - downTime
            val gestureType = classifier.onElapsedTime(elapsedMs)
            if (gestureType == GestureType.LONG_PRESS) {
                longPressInjected = true
                onLongPressCallback?.invoke()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                lastMoveTime = event.eventTime
                downTime = System.currentTimeMillis()
                isMoving = false
                longPressInjected = false
                classifier.onDown()

                // Schedule long-press check
                handler.postDelayed(
                    longPressCheckRunnable,
                    TouchGestureClassifier.LONG_PRESS_DURATION_THRESHOLD_MS,
                )
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
                    // Cancel long-press check — finger moved too far
                    handler.removeCallbacks(longPressCheckRunnable)
                    onDragCallback?.invoke(dx, dy, speed)
                }
                lastTouchX = event.x
                lastTouchY = event.y
                lastMoveTime = event.eventTime
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Cancel any pending long-press check
                handler.removeCallbacks(longPressCheckRunnable)

                val elapsedMs = System.currentTimeMillis() - downTime
                val gestureType = classifier.onUp(elapsedMs)

                when (gestureType) {
                    GestureType.TAP -> onTapCallback?.invoke()
                    GestureType.LONG_PRESS -> {
                        // Only inject if not already injected via onElapsedTime
                        if (!longPressInjected) {
                            onLongPressCallback?.invoke()
                        }
                    }
                    GestureType.MOVE -> { /* No injection on release after move */ }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(longPressCheckRunnable)
    }
}
