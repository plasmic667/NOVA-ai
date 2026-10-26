package com.example.nova.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.data.preferences.NovaPreferences
import com.example.nova.firebase.AuthRepository
import com.example.nova.repository.AssistantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = NovaPreferences(application)
    private val assistantRepository = AssistantRepository(application)
    private val authRepository = AuthRepository(application)

    val syncToCloud: StateFlow<Boolean> = preferences.syncToCloudFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val voiceAutoPlay: StateFlow<String> = preferences.voiceAutoPlayFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "VOICE_ONLY")

    val speechSpeed: StateFlow<Float> = preferences.speechSpeedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 1.0f)

    val agentMode: StateFlow<Boolean> = preferences.agentModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val highThinking: StateFlow<Boolean> = preferences.highThinkingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    fun setSyncToCloud(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setSyncToCloud(enabled)
        }
    }

    fun setVoiceAutoPlay(mode: String) {
        viewModelScope.launch {
            preferences.setVoiceAutoPlay(mode)
        }
    }

    fun setSpeechSpeed(speed: Float) {
        viewModelScope.launch {
            preferences.setSpeechSpeed(speed)
        }
    }

    fun setAgentMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAgentMode(enabled)
        }
    }

    fun setHighThinking(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setHighThinking(enabled)
        }
    }

    fun clearLocalData(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            assistantRepository.clearLocalData()
            onDone()
        }
    }

    fun deleteAccountAndData(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val uid = authRepository.getUserId()
            if (uid.isNotBlank()) {
                assistantRepository.deleteUserCloudAndLocalData(uid)
            }
            authRepository.deleteAccount()
            preferences.clearPreferences()
            onDone()
        }
    }
}
