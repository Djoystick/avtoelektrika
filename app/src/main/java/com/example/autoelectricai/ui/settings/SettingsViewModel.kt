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

    // BUGFIX: Do NOT snapshot auth.currentUser here — it's a race condition on cold start.
    // Firebase restores the session asynchronously; AuthStateListener below handles all updates.
    private val _expertEmail = MutableStateFlow("")
    val expertEmail = _expertEmail.asStateFlow()

    // Start as false; AuthStateListener will update to true when session is restored
    private val _isExpertLoggedIn = MutableStateFlow(false)
    val isExpertLoggedIn = _isExpertLoggedIn.asStateFlow()

    // True while we're waiting for Firebase to restore the session on cold start
    private val _isAuthReady = MutableStateFlow(false)
    val isAuthReady = _isAuthReady.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val _userRole = MutableStateFlow("user")
    val userRole = _userRole.asStateFlow()

    private val _userKarma = MutableStateFlow(0)
    val userKarma = _userKarma.asStateFlow()

    private val _adminActionMessage = MutableStateFlow<String?>(null)
    val adminActionMessage = _adminActionMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        // Mark auth as ready regardless of whether user is logged in or anonymous
        _isAuthReady.value = true
        if (user != null && !user.isAnonymous && !user.email.isNullOrBlank()) {
            val email = user.email!!
            _expertEmail.value = email
            _isExpertLoggedIn.value = true
            viewModelScope.launch {
                fetchUserRole(email)
            }
        } else {
            _expertEmail.value = ""
            _isExpertLoggedIn.value = false
            _userRole.value = "user"
            _userKarma.value = 0
        }
    }

    init {
        viewModelScope.launch {
            _geminiKey.value = settings.geminiApiKey.first()
            _geminiProxyUrl.value = settings.geminiProxyUrl.first()
            _openAiKey.value = settings.openAiApiKey.first()
            _preferredAi.value = settings.preferredAi.first()
        }
        // AuthStateListener fires immediately with cached session if one exists,
        // so this handles both cold start restoration and new logins.
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
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

    fun loginExpert(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                val currentEmail = com.example.autoelectricai.utils.AuthUtils.currentUserEmail
                _expertEmail.value = currentEmail
                _isExpertLoggedIn.value = true
                
                if (currentEmail.isNotBlank()) {
                    fetchUserRole(currentEmail)
                }
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Ошибка авторизации"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun logoutExpert() {
        auth.signOut() // это разлогинивает, но мы хотим остаться анонимными
        // Поэтому сразу логинимся анонимно опять
        viewModelScope.launch {
            try {
                auth.signInAnonymously().await()
            } catch (e: Exception) { }
            _expertEmail.value = ""
            _isExpertLoggedIn.value = false
            _userRole.value = "user"
            _userKarma.value = 0
        }
    }

    fun grantRole(email: String, role: String) {
        viewModelScope.launch {
            if (email.isBlank()) return@launch
            cloudSyncRepo.setRole(email, role)
            _adminActionMessage.value = "Роль '$role' выдана для $email"
        }
    }

    fun revokeRole(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) return@launch
            cloudSyncRepo.deleteRole(email)
            _adminActionMessage.value = "Права забраны у $email"
        }
    }
    
    fun clearAdminMessage() {
        _adminActionMessage.value = null
    }
}
