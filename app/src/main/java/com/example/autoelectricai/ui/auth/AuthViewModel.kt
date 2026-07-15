package com.example.autoelectricai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.sync.CloudSyncRepository
import com.example.autoelectricai.utils.AppLogger
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val cloudSync: CloudSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Заполните все поля")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                AppLogger.i("AuthViewModel", "Signed in as: ${auth.currentUser?.email}")
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                AppLogger.e("AuthViewModel", "Login failed", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Ошибка авторизации")
            }
        }
    }

    fun registerWithEmail(email: String, pass: String, passConfirm: String) {
        if (email.isBlank() || pass.isBlank() || passConfirm.isBlank()) {
            _uiState.value = AuthUiState.Error("Заполните все поля")
            return
        }
        if (pass != passConfirm) {
            _uiState.value = AuthUiState.Error("Пароли не совпадают")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                AppLogger.i("AuthViewModel", "Registered as: ${auth.currentUser?.email}")
                // Set default role
                cloudSync.setRole(email, "user")
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                AppLogger.e("AuthViewModel", "Registration failed", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Ошибка регистрации")
            }
        }
    }

    fun signInAnonymously() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                cloudSync.signInAnonymouslyIfNeeded()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                AppLogger.e("AuthViewModel", "Anonymous login failed", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Ошибка гостевого входа")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
