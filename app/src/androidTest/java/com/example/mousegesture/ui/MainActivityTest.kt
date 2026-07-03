package com.example.mousegesture.ui

import android.provider.Settings
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mousegesture.MainActivity
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun clickOpenAccessibilitySettings_launchesAccessibilitySettingsIntent() {
        // The button should be visible when service is not enabled (default state in test)
        composeTestRule.onNodeWithText("Open Accessibility Settings")
            .performClick()

        Intents.intended(
            allOf(
                hasAction(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            )
        )
    }
}
