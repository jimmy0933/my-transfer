package com.example.cameratranslator.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val TRANSPARENCY_KEY = floatPreferencesKey("translation_box_transparency")

    val transparencyFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[TRANSPARENCY_KEY] ?: 0.5f
        }

    suspend fun saveTransparency(transparency: Float) {
        context.dataStore.edit { preferences ->
            preferences[TRANSPARENCY_KEY] = transparency
        }
    }
}
