package com.bankyar.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore("theme_prefs")

class DarkModeManager(private val context: Context) {
    companion object {
        private val DARK_KEY = booleanPreferencesKey("dark_mode")
    }
    val isDarkMode: Flow<Boolean> = context.themeDataStore.data.map { it[DARK_KEY] ?: false }
    suspend fun toggle() = context.themeDataStore.edit { it[DARK_KEY] = !(it[DARK_KEY] ?: false) }
    suspend fun set(enabled: Boolean) = context.themeDataStore.edit { it[DARK_KEY] = enabled }
}
