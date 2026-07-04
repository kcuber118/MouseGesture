package com.example.mousegesture.domain.preferences

/**
 * Repository interface for persisting and retrieving user preferences.
 *
 * Domain interface — no Android framework imports.
 * Implementation lives in the infrastructure layer.
 */
interface PreferencesRepository {
    /**
     * Get the current user preferences.
     * Returns default values if nothing has been saved yet.
     */
    suspend fun getPreferences(): UserPreferences

    /**
     * Save user preferences.
     */
    suspend fun savePreferences(prefs: UserPreferences)

    /**
     * Stream of preference changes (for live sensitivity updates).
     */
    fun preferencesFlow(): kotlinx.coroutines.flow.Flow<UserPreferences>
}
