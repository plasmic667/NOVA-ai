package com.example.nova.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.firebase.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Attempt silent auto-sign in upon launch
        authRepository.attemptAutoSignIn(viewModelScope)
    }

    fun signInWithGoogle(activity: Activity) {
        _isLoading.value = true
        _errorMessage.value = null
        authRepository.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = {
                _isLoading.value = false
            },
            onError = { error ->
                _isLoading.value = false
                _errorMessage.value = error
            },
            onCancelled = {
                _isLoading.value = false
            }
        )
    }

    fun signOut(onComplete: () -> Unit = {}) {
        _isLoading.value = true
        authRepository.signOut(viewModelScope) {
            _isLoading.value = false
            onComplete()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
