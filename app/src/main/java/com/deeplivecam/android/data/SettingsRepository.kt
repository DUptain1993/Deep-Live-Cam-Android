package com.deeplivecam.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.deeplivecam.android.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Settings Repository using DataStore
 * Persists user preferences for the app
 */
class SettingsRepository private constructor(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = Constants.PREF_NAME
    )
    
    // Preference keys
    private object PreferencesKeys {
        val MANY_FACES = booleanPreferencesKey(Constants.KEY_MANY_FACES)
        val MOUTH_MASK = booleanPreferencesKey(Constants.KEY_MOUTH_MASK)
        val FACE_ENHANCEMENT = booleanPreferencesKey(Constants.KEY_ENHANCEMENT)
        val MIRROR_CAMERA = booleanPreferencesKey(Constants.KEY_MIRROR)
        val NSFW_FILTER = booleanPreferencesKey(Constants.KEY_NSFW_FILTER)
        val EXECUTION_PROVIDER = stringPreferencesKey(Constants.KEY_EXECUTION_PROVIDER)
        val OUTPUT_QUALITY = floatPreferencesKey(Constants.KEY_QUALITY)
        val RESOLUTION = stringPreferencesKey(Constants.KEY_RESOLUTION)
        val TARGET_FPS = stringPreferencesKey(Constants.KEY_TARGET_FPS)
    }
    
    // Settings data class
    data class AppSettings(
        val manyFaces: Boolean = false,
        val mouthMask: Boolean = false,
        val faceEnhancement: Boolean = false,
        val mirrorCamera: Boolean = false,
        val nsfwFilter: Boolean = true,
        val executionProvider: String = Constants.EXEC_NNAPI,
        val outputQuality: Float = Constants.DEFAULT_QUALITY,
        val resolution: String = "720p",
        val targetFps: String = "24"
    )
    
    /**
     * Get settings as Flow
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                manyFaces = preferences[PreferencesKeys.MANY_FACES] ?: false,
                mouthMask = preferences[PreferencesKeys.MOUTH_MASK] ?: false,
                faceEnhancement = preferences[PreferencesKeys.FACE_ENHANCEMENT] ?: false,
                mirrorCamera = preferences[PreferencesKeys.MIRROR_CAMERA] ?: false,
                nsfwFilter = preferences[PreferencesKeys.NSFW_FILTER] ?: true,
                executionProvider = preferences[PreferencesKeys.EXECUTION_PROVIDER] 
                    ?: Constants.EXEC_NNAPI,
                outputQuality = preferences[PreferencesKeys.OUTPUT_QUALITY] 
                    ?: Constants.DEFAULT_QUALITY,
                resolution = preferences[PreferencesKeys.RESOLUTION] ?: "720p",
                targetFps = preferences[PreferencesKeys.TARGET_FPS] ?: "24"
            )
        }
    
    /**
     * Update many faces setting
     */
    suspend fun setManyFaces(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MANY_FACES] = enabled
        }
    }
    
    /**
     * Update mouth mask setting
     */
    suspend fun setMouthMask(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOUTH_MASK] = enabled
        }
    }
    
    /**
     * Update face enhancement setting
     */
    suspend fun setFaceEnhancement(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FACE_ENHANCEMENT] = enabled
        }
    }
    
    /**
     * Update mirror camera setting
     */
    suspend fun setMirrorCamera(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIRROR_CAMERA] = enabled
        }
    }
    
    /**
     * Update NSFW filter setting
     */
    suspend fun setNsfwFilter(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NSFW_FILTER] = enabled
        }
    }
    
    /**
     * Update execution provider
     */
    suspend fun setExecutionProvider(provider: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXECUTION_PROVIDER] = provider
        }
    }
    
    /**
     * Update output quality
     */
    suspend fun setOutputQuality(quality: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OUTPUT_QUALITY] = quality
        }
    }
    
    suspend fun setResolution(resolution: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RESOLUTION] = resolution
        }
    }
    
    suspend fun setTargetFps(fps: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_FPS] = fps
        }
    }
    
    /**
     * Reset all settings to defaults
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    companion object {
        @Volatile
        private var instance: SettingsRepository? = null
        
        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
