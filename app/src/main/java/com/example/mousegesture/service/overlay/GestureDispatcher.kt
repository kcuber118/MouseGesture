package com.example.mousegesture.service.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.os.Handler

/**
 * Interface wrapping [AccessibilityService.dispatchGesture] so it can be mocked in tests.
 * The real implementation delegates to the AccessibilityService.
 */
interface GestureDispatcher {
    fun dispatchGesture(
        gesture: GestureDescription,
        callback: AccessibilityService.GestureResultCallback?,
        handler: Handler?,
    ): Boolean
}
