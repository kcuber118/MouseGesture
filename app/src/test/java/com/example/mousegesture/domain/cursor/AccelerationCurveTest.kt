package com.example.mousegesture.domain.cursor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccelerationCurveTest {

    // --- Tracer bullet: acceleration curve gives different output for different speeds ---

    @Test
    fun apply_sameDeltaAtHigherSpeed_givesLargerOutput() {
        val curve = AccelerationCurve()

        // Same delta (10px), but different speeds (elapsed time)
        // Slow swipe: 10px in 100ms → speed = 0.1 px/ms
        // Fast swipe: 10px in 10ms → speed = 1.0 px/ms
        val slowOutput = curve.apply(delta = 10f, speed = 0.1f)
        val fastOutput = curve.apply(delta = 10f, speed = 1.0f)

        // Fast swipe should produce larger cursor movement than slow swipe
        assertTrue(
            "Fast swipe ($fastOutput) should move cursor more than slow swipe ($slowOutput)",
            fastOutput > slowOutput,
        )
    }

    @Test
    fun apply_zeroDelta_returnsZero() {
        val curve = AccelerationCurve()
        assertEquals(0f, curve.apply(delta = 0f, speed = 1.0f), 0.001f)
    }

    @Test
    fun apply_negativeDelta_staysNegative() {
        val curve = AccelerationCurve()

        // Negative delta (swipe left/up) should produce negative output
        val output = curve.apply(delta = -10f, speed = 0.5f)
        assertTrue("Negative delta should produce negative output, got $output", output < 0f)
    }

    @Test
    fun apply_zeroSpeed_fallsBackToLinear() {
        val curve = AccelerationCurve()

        // Zero speed (no movement info) → linear 1:1 as fallback
        val output = curve.apply(delta = 10f, speed = 0f)
        assertEquals(10f, output, 0.001f)
    }

    @Test
    fun apply_slowSpeed_outputLessThanDelta() {
        val curve = AccelerationCurve()

        // Very slow swipe: output should be less than raw delta (precision mode)
        val output = curve.apply(delta = 10f, speed = 0.05f)
        assertTrue(
            "Slow swipe output ($output) should be less than delta (10)",
            output < 10f,
        )
    }

    @Test
    fun apply_fastSpeed_outputMoreThanDelta() {
        val curve = AccelerationCurve()

        // Fast swipe: output should be more than raw delta (boost mode)
        val output = curve.apply(delta = 10f, speed = 2.0f)
        assertTrue(
            "Fast swipe output ($output) should be more than delta (10)",
            output > 10f,
        )
    }

    // --- Sensitivity multiplier ---

    @Test
    fun apply_higherSensitivity_givesLargerOutput() {
        val lowSensitivity = AccelerationCurve(sensitivity = 0.5f)
        val highSensitivity = AccelerationCurve(sensitivity = 2.0f)

        val lowOutput = lowSensitivity.apply(delta = 10f, speed = 1.0f)
        val highOutput = highSensitivity.apply(delta = 10f, speed = 1.0f)

        assertTrue(
            "High sensitivity ($highOutput) should produce more than low ($lowOutput)",
            highOutput > lowOutput,
        )
    }

    @Test
    fun apply_sensitivityOne_matchesDefault() {
        val curve = AccelerationCurve(sensitivity = 1.0f)
        val defaultCurve = AccelerationCurve()

        val output = curve.apply(delta = 10f, speed = 0.5f)
        val defaultOutput = defaultCurve.apply(delta = 10f, speed = 0.5f)

        assertEquals(defaultOutput, output, 0.001f)
    }

    @Test
    fun apply_sensitivityScalesLinearly() {
        val curve1x = AccelerationCurve(sensitivity = 1.0f)
        val curve2x = AccelerationCurve(sensitivity = 2.0f)

        val output1x = curve1x.apply(delta = 10f, speed = 1.0f)
        val output2x = curve2x.apply(delta = 10f, speed = 1.0f)

        // 2x sensitivity should produce exactly 2x the output
        assertEquals(output1x * 2f, output2x, 0.001f)
    }

    @Test
    fun apply_changingSensitivityDoesNotAffectSign() {
        val curve = AccelerationCurve(sensitivity = 3.0f)

        val negativeOutput = curve.apply(delta = -10f, speed = 0.5f)
        assertTrue("Negative delta with high sensitivity should stay negative", negativeOutput < 0f)
    }
}
