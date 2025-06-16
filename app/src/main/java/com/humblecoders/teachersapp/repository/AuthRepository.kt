package com.humblecoders.teachersapp.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.humblecoders.teachersapp.model.TeacherData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "teacher_prefs")

class AuthRepository(private val context: Context) {

    private val teacherDataKey = stringPreferencesKey("teacher_data")
    private val cachedRoomsKey = stringPreferencesKey("cached_rooms")

    suspend fun saveTeacherData(teacherData: TeacherData) {
        val json = Json.encodeToString(teacherData)
        context.dataStore.edit { preferences ->
            preferences[teacherDataKey] = json
        }
    }

    suspend fun getTeacherData(): TeacherData? {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[teacherDataKey]
            }.first()

            json?.let { Json.decodeFromString<TeacherData>(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteTeacherData() {
        context.dataStore.edit { preferences ->
            preferences.remove(teacherDataKey)
        }
    }

    suspend fun saveCachedRooms(rooms: List<String>) {
        val json = Json.encodeToString(rooms)
        context.dataStore.edit { preferences ->
            preferences[cachedRoomsKey] = json
        }
    }

    suspend fun getCachedRooms(): List<String> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[cachedRoomsKey]
            }.first()

            json?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}