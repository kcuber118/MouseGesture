package com.example.mousegesture.domain.cursor

import kotlin.math.pow
import kotlin.math.sign

/**
 * Applies an acceleration curve with sensitivity to touchpad movement deltas.
 *
 * The curve is a power function: output = sensitivity * sign(delta) * |delta| * speed^(exponent-1)
 *
 * - Slow swipe (low speed) → output < delta (precision mode)
 * - Fast swipe (high speed) → output > delta (boost mode)
 * - Zero speed → linear 1:1 fallback (then scaled by sensitivity)
 * - Sensitivity scales the output linearly
 *
 * Domain logic — no Android framework imports.
 */
class AccelerationCurve(
    /** Power exponent for the acceleration curve. Higher = more aggressive acceleration. */
    private val exponent: Float = DEFAULT_EXPONENT,
    /** Linear sensitivity multiplier. 1.0 = default, >1.0 = more responsive, <1.0 = less. */
    private val sensitivity: Float = DEFAULT_SENSITIVITY,
) {

    companion object {
        /** Default power exponent. Values > 1.0 give acceleration. */
        const val DEFAULT_EXPONENT = 1.5f
        /** Default sensitivity multiplier. */
        const val DEFAULT_SENSITIVITY = 1.0f
    }

    /**
     * Apply acceleration to a movement delta.
     *
     * @param delta Raw touch delta in pixels.
     * @param speed Movement speed in px/ms (|delta| / elapsed time).
     * @return Accelerated delta in pixels, same sign as input, scaled by sensitivity.
     */
    fun apply(delta: Float, speed: Float): Float {
        if (delta == 0f) return 0f
        if (speed == 0f) return delta * sensitivity // linear fallback, still apply sensitivity

        val absDelta = delta.absoluteValue
        val magnitude = absDelta * speed.pow(exponent - 1f)
        return sign(delta) * magnitude * sensitivity
    }

    /**
     * Create a copy with a different sensitivity.
     * Used for live adjustment without changing exponent.
     */
    fun withSensitivity(newSensitivity: Float): AccelerationCurve {
        return AccelerationCurve(exponent = exponent, sensitivity = newSensitivity)
    }
}

private val Float.absoluteValue: Float
    get() = if (this < 0f) -this else this
