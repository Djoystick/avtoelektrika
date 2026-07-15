package com.example.autoelectricai.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.R
import com.example.autoelectricai.data.sync.CloudSyncRepository
import com.example.autoelectricai.utils.AppLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    fun signInWithGoogle(context: Context) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                // Для работы нужен Web Client ID, который Firebase создает при активации Google Sign In.
                // Пока мы используем заглушку, которая будет заменена на реальный ключ из strings.xml
                // Заглушка не сработает в проде, но даст скомпилировать код и покажет нужный Exception.
                val webClientId = "YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com"
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential).await()
                    AppLogger.i("AuthViewModel", "Google sign in success: ${auth.currentUser?.email}")
                    
                    val email = auth.currentUser?.email ?: ""
                    if (email.isNotBlank()) {
                        // Ensure role exists
                        val currentRole = cloudSync.getUserRole(email)
                        if (currentRole == "user") {
                            cloudSync.setRole(email, "user")
                        }
                    }
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error("Неподдерживаемый тип авторизации Google")
                }
            } catch (e: Exception) {
                AppLogger.e("AuthViewModel", "Google sign in failed", e)
                _uiState.value = AuthUiState.Error("Для работы Google Auth нужен Client ID из Firebase Console.")
            }
        }
    }

    fun startTelegramAuth(context: Context) {
        _uiState.value = AuthUiState.Loading
        
        val sessionId = java.util.UUID.randomUUID().toString()
        val botUsername = "AutoElectricalENC_bot"
        
        // 1. Открываем Telegram с передачей sessionId
        try {
            val uri = android.net.Uri.parse("tg://resolve?domain=$botUsername&start=$sessionId")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLogger.e("AuthViewModel", "Не удалось открыть Telegram", e)
            _uiState.value = AuthUiState.Error("Telegram не установлен или произошла ошибка")
            return
        }

        // 2. Начинаем слушать Firestore по этому sessionId
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val docRef = db.collection("telegram_auth").document(sessionId)
        
        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        
        listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                AppLogger.e("AuthViewModel", "Ошибка прослушивания Telegram Auth", e)
                _uiState.value = AuthUiState.Error("Ошибка связи с сервером")
                listenerRegistration?.remove()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val customToken = snapshot.getString("customToken")
                if (customToken != null) {
                    listenerRegistration?.remove() // Больше не слушаем
                    
                    // 3. Авторизуемся с помощью полученного Custom Token
                    viewModelScope.launch {
                        try {
                            auth.signInWithCustomToken(customToken).await()
                            AppLogger.i("AuthViewModel", "Telegram sign in success: ${auth.currentUser?.uid}")
                            
                            val email = auth.currentUser?.email ?: ""
                            val telegramId = snapshot.getLong("telegramId")?.toString() ?: ""
                            val fakeEmail = if (email.isBlank()) "tg_$telegramId@telegram.auth" else email
                            
                            // Убеждаемся, что у пользователя есть роль
                            val currentRole = cloudSync.getUserRole(fakeEmail)
                            if (currentRole == "user") {
                                cloudSync.setRole(fakeEmail, "user")
                            }
                            
                            // Если у пользователя нет никнейма, можно попытаться поставить ему firstName
                            val currentNickname = cloudSync.getNickname(fakeEmail)
                            if (currentNickname == null) {
                                val firstName = snapshot.getString("firstName")
                                if (firstName != null && cloudSync.checkNicknameUnique(firstName, fakeEmail)) {
                                    cloudSync.setNickname(fakeEmail, firstName)
                                }
                            }
                            
                            _uiState.value = AuthUiState.Success
                        } catch (ex: Exception) {
                            AppLogger.e("AuthViewModel", "Ошибка входа по Custom Token", ex)
                            _uiState.value = AuthUiState.Error("Ошибка авторизации: ${ex.localizedMessage}")
                        }
                    }
                }
            }
        }
        
        // Timeout через 5 минут (300 секунд)
        viewModelScope.launch {
            kotlinx.coroutines.delay(300000)
            if (_uiState.value is AuthUiState.Loading) {
                listenerRegistration?.remove()
                _uiState.value = AuthUiState.Error("Время ожидания авторизации Telegram истекло")
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
