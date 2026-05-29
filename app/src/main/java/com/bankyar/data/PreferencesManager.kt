package com.bankyar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("bankyar_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")
    }

    val loggedInUserId: Flow<Int> = context.dataStore.data.map { it[LOGGED_IN_USER_ID] ?: -1 }

    suspend fun saveUserId(id: Int) {
        context.dataStore.edit { it[LOGGED_IN_USER_ID] = id }
    }

    suspend fun clearUserId() {
        context.dataStore.edit { it.remove(LOGGED_IN_USER_ID) }
    }
}
