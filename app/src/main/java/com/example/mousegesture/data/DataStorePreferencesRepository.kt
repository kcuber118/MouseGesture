package com.example.mousegesture.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mousegesture.domain.cursor.AccelerationCurve
import com.example.mousegesture.domain.model.Point
import com.example.mousegesture.domain.preferences.PreferencesRepository
import com.example.mousegesture.domain.preferences.UserPreferences
import com.example.mousegesture.domain.touchpad.TouchpadRect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore Preferences implementation of [PreferencesRepository].
 */
class DataStorePreferencesRepository(
    private val context: Context,
) : PreferencesRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "mouse_gesture_prefs",
    )

    companion object {
        private val KEY_SENSITIVITY = floatPreferencesKey("sensitivity")
        private val KEY_OVERLAY_VISIBLE = booleanPreferencesKey("overlay_visible")
        private val KEY_TOUCHPAD_LEFT = floatPreferencesKey("touchpad_left")
        private val KEY_TOUCHPAD_TOP = floatPreferencesKey("touchpad_top")
        private val KEY_TOUCHPAD_RIGHT = floatPreferencesKey("touchpad_right")
        private val KEY_TOUCHPAD_BOTTOM = floatPreferencesKey("touchpad_bottom")
        private val KEY_CURSOR_X = floatPreferencesKey("cursor_x")
        private val KEY_CURSOR_Y = floatPreferencesKey("cursor_y")
    }

    override suspend fun getPreferences(): UserPreferences {
        val stored = context.dataStore.data.first()
        return mapToUserPreferences(stored)
    }

    override suspend fun savePreferences(prefs: UserPreferences) {
        context.dataStore.edit { stored ->
            stored[KEY_SENSITIVITY] = prefs.sensitivity
            stored[KEY_OVERLAY_VISIBLE] = prefs.overlayVisible
            prefs.touchpadRect?.let { rect ->
                stored[KEY_TOUCHPAD_LEFT] = rect.left
                stored[KEY_TOUCHPAD_TOP] = rect.top
                stored[KEY_TOUCHPAD_RIGHT] = rect.right
                stored[KEY_TOUCHPAD_BOTTOM] = rect.bottom
            }
            prefs.cursorPosition?.let { pos ->
                stored[KEY_CURSOR_X] = pos.x
                stored[KEY_CURSOR_Y] = pos.y
            }
        }
    }

    override fun preferencesFlow(): Flow<UserPreferences> {
        return context.dataStore.data.map { stored ->
            mapToUserPreferences(stored)
        }
    }

    private fun mapToUserPreferences(stored: Preferences): UserPreferences {
        val sensitivity = stored[KEY_SENSITIVITY] ?: AccelerationCurve.DEFAULT_SENSITIVITY
        val overlayVisible = stored[KEY_OVERLAY_VISIBLE] ?: true

        val touchpadRect = if (
            stored[KEY_TOUCHPAD_LEFT] != null &&
            stored[KEY_TOUCHPAD_TOP] != null &&
            stored[KEY_TOUCHPAD_RIGHT] != null &&
            stored[KEY_TOUCHPAD_BOTTOM] != null
        ) {
            TouchpadRect(
                left = stored[KEY_TOUCHPAD_LEFT]!!,
                top = stored[KEY_TOUCHPAD_TOP]!!,
                right = stored[KEY_TOUCHPAD_RIGHT]!!,
                bottom = stored[KEY_TOUCHPAD_BOTTOM]!!,
            )
        } else null

        val cursorPosition = if (
            stored[KEY_CURSOR_X] != null &&
            stored[KEY_CURSOR_Y] != null
        ) {
            Point(stored[KEY_CURSOR_X]!!, stored[KEY_CURSOR_Y]!!)
        } else null

        return UserPreferences(
            sensitivity = sensitivity,
            overlayVisible = overlayVisible,
            touchpadRect = touchpadRect,
            cursorPosition = cursorPosition,
        )
    }
}
