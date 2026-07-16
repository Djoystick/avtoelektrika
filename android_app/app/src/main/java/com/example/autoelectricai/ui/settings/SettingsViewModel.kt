package com.example.autoelectricai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.prefs.SettingsRepository
import com.example.autoelectricai.data.sync.CloudSyncRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.autoelectricai.data.update.AppUpdateManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val auth: FirebaseAuth,
    private val cloudSyncRepo: CloudSyncRepository,
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    private val _geminiKey = MutableStateFlow("")
    val geminiKey = _geminiKey.asStateFlow()
    fun setGeminiKey(key: String) { _geminiKey.value = key }

    private val _geminiProxyUrl = MutableStateFlow("")
    val geminiProxyUrl = _geminiProxyUrl.asStateFlow()
    fun setGeminiProxyUrl(url: String) { _geminiProxyUrl.value = url }

    private val _openAiKey = MutableStateFlow("")
    val openAiKey = _openAiKey.asStateFlow()
    fun setOpenAiKey(key: String) { _openAiKey.value = key }

    private val _preferredAi = MutableStateFlow("gemini")
    val preferredAi = _preferredAi.asStateFlow()
    fun setPreferredAi(ai: String) { _preferredAi.value = ai }

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val _userRole = MutableStateFlow("user")
    val userRole = _userRole.asStateFlow()

    private val _userKarma = MutableStateFlow(0)
    val userKarma = _userKarma.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            _geminiKey.value = settings.geminiApiKey.first()
            _geminiProxyUrl.value = settings.geminiProxyUrl.first()
            _openAiKey.value = settings.openAiApiKey.first()
            _preferredAi.value = settings.preferredAi.first()
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun saveSettings() {
        viewModelScope.launch {
            settings.saveGeminiKey(_geminiKey.value.trim())
            settings.saveGeminiProxyUrl(_geminiProxyUrl.value.trim())
            settings.saveOpenAiKey(_openAiKey.value.trim())
            settings.savePreferredAi(_preferredAi.value)
            _saved.value = true
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                cloudSyncRepo.pullCommunityUpdates()
            } catch (e: Exception) {
                // ignore
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private suspend fun fetchUserRole(email: String) {
        // Bootstrap Masters
        if (email == "j.j.niccals2@gmail.com" || email == "slepa4ok@mail.ru") {
            cloudSyncRepo.setRole(email, "admin")
        }
        _userRole.value = cloudSyncRepo.getUserRole(email)
        _userKarma.value = cloudSyncRepo.getUserKarma(email)
    }
}
