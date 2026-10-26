package com.example.nova.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.novaDataStore by preferencesDataStore(name = "nova_settings")

class NovaPreferences(private val context: Context) {

    companion object {
        val KEY_SYNC_TO_CLOUD = booleanPreferencesKey("sync_to_cloud")
        val KEY_VOICE_AUTOPLAY = stringPreferencesKey("voice_autoplay") // ALWAYS, VOICE_ONLY, NEVER
        val KEY_SPEECH_SPEED = floatPreferencesKey("speech_speed")
        val KEY_AGENT_MODE = booleanPreferencesKey("agent_mode_enabled")
        val KEY_HIGH_THINKING = booleanPreferencesKey("high_thinking_enabled")
    }

    val syncToCloudFlow: Flow<Boolean> = context.novaDataStore.data.map { prefs ->
        prefs[KEY_SYNC_TO_CLOUD] ?: true
    }

    val voiceAutoPlayFlow: Flow<String> = context.novaDataStore.data.map { prefs ->
        prefs[KEY_VOICE_AUTOPLAY] ?: "VOICE_ONLY"
    }

    val speechSpeedFlow: Flow<Float> = context.novaDataStore.data.map { prefs ->
        prefs[KEY_SPEECH_SPEED] ?: 1.0f
    }

    val agentModeFlow: Flow<Boolean> = context.novaDataStore.data.map { prefs ->
        prefs[KEY_AGENT_MODE] ?: true
    }

    val highThinkingFlow: Flow<Boolean> = context.novaDataStore.data.map { prefs ->
        prefs[KEY_HIGH_THINKING] ?: false
    }

    suspend fun setSyncToCloud(enabled: Boolean) {
        context.novaDataStore.edit { prefs ->
            prefs[KEY_SYNC_TO_CLOUD] = enabled
        }
    }

    suspend fun setVoiceAutoPlay(mode: String) {
        context.novaDataStore.edit { prefs ->
            prefs[KEY_VOICE_AUTOPLAY] = mode
        }
    }

    suspend fun setSpeechSpeed(speed: Float) {
        context.novaDataStore.edit { prefs ->
            prefs[KEY_SPEECH_SPEED] = speed
        }
    }

    suspend fun setAgentMode(enabled: Boolean) {
        context.novaDataStore.edit { prefs ->
            prefs[KEY_AGENT_MODE] = enabled
        }
    }

    suspend fun setHighThinking(enabled: Boolean) {
        context.novaDataStore.edit { prefs ->
            prefs[KEY_HIGH_THINKING] = enabled
        }
    }

    suspend fun clearPreferences() {
        context.novaDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
