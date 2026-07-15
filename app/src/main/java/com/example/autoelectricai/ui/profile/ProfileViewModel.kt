package com.example.autoelectricai.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.sync.CloudSyncRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

import com.example.autoelectricai.R

data class AwardInfo(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int
)

val AVAILABLE_AWARDS = listOf(
    // Ветвь 1: Исследователь
    AwardInfo("first_scan", "Первая Диагностика", "Сохранено 1 локальное решение.", R.drawable.ic_award_first_scan),
    AwardInfo("seeker_of_truth", "Искатель Истины", "Сохранено 5 локальных решений.", R.drawable.ic_award_seeker),
    AwardInfo("mechanic_apprentice", "Ученик Механика", "Сохранено 10 локальных решений.", R.drawable.ic_award_apprentice),
    AwardInfo("cyber_diagnost", "Кибер-Диагност", "Сохранено 20 локальных решений.", R.drawable.ic_award_cyber),
    AwardInfo("ecu_oracle", "Оракул ЭБУ", "Сохранено 50 локальных решений.", R.drawable.ic_award_oracle),
    AwardInfo("system_architect", "Системный Архитектор", "Сохранено 100 локальных решений.", R.drawable.ic_award_architect),
    
    // Ветвь 2: Наставник
    AwardInfo("first_blood", "Первая Кровь", "Первый полученный лайк за решение.", R.drawable.ic_award_first_blood),
    AwardInfo("helpful_neighbor", "Добрый Сосед", "Достигнуто 10 Кармы.", R.drawable.ic_award_neighbor),
    AwardInfo("voice_of_people", "Голос Народа", "Достигнуто 50 Кармы.", R.drawable.ic_award_voice),
    AwardInfo("respected_master", "Уважаемый Мастер", "Достигнуто 100 Кармы.", R.drawable.ic_award_master),
    AwardInfo("knowledge_patron", "Меценат Знаний", "Достигнуто 200 Кармы.", R.drawable.ic_award_patron),
    AwardInfo("community_pillar", "Столп Сообщества", "Достигнуто 500 Кармы.", R.drawable.ic_award_pillar),
    AwardInfo("garage_legend", "Легенда Гаражей", "Достигнуто 1000 Кармы.", R.drawable.ic_award_legend),
    
    // Ветвь 3: Хранитель
    AwardInfo("eagle_eye", "Зоркий Глаз", "Внесено 1 дополнение к чужому решению.", R.drawable.ic_award_eye),
    AwardInfo("detail_hunter", "Охотник за Деталями", "Внесено 5 полезных дополнений.", R.drawable.ic_award_hunter),
    AwardInfo("chief_archivist", "Главный Архивариус", "Внесено 15 полезных дополнений в базу.", R.drawable.ic_award_archivist),
    AwardInfo("omniscient", "Всезнающий", "Внесено 50 полезных дополнений.", R.drawable.ic_award_omniscient),
    AwardInfo("gate_guard", "Страж Врат", "Одобрено 1 чужое решение на модерации.", R.drawable.ic_award_guard),
    AwardInfo("base_keeper", "Хранитель Базы", "Одобрено 10 чужих решений.", R.drawable.ic_award_keeper),
    AwardInfo("grand_inquisitor", "Великий Инквизитор", "Одобрено 50 чужих решений.", R.drawable.ic_award_inquisitor)
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val cloudSyncRepo: CloudSyncRepository
) : ViewModel() {

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userRole = MutableStateFlow("user")
    val userRole = _userRole.asStateFlow()

    private val _userKarma = MutableStateFlow(0)
    val userKarma = _userKarma.asStateFlow()

    private val _userAwards = MutableStateFlow<List<AwardInfo>>(emptyList())
    val userAwards = _userAwards.asStateFlow()

    private val _displayedAwards = MutableStateFlow<List<AwardInfo>>(emptyList())
    val displayedAwards = _displayedAwards.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    private val _isOwnProfile = MutableStateFlow(true)
    val isOwnProfile = _isOwnProfile.asStateFlow()

    private val _nicknameError = MutableStateFlow<String?>(null)
    val nicknameError = _nicknameError.asStateFlow()

    private val _isLoadingNickname = MutableStateFlow(false)
    val isLoadingNickname = _isLoadingNickname.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null && !user.isAnonymous && !user.email.isNullOrBlank()) {
            loadProfile(user.email)
        } else {
            _userEmail.value = ""
            _isOwnProfile.value = false
            _userRole.value = "user"
            _userKarma.value = 0
            _userName.value = null
            _userAwards.value = emptyList()
            _displayedAwards.value = emptyList()
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun loadProfile(emailOverride: String?) {
        val currentEmail = auth.currentUser?.email ?: ""
        val email = emailOverride ?: currentEmail
        
        _userEmail.value = email
        _isOwnProfile.value = (email == currentEmail && currentEmail.isNotBlank())

        if (email.isNotBlank()) {
            viewModelScope.launch {
                _isLoadingNickname.value = true
                cloudSyncRepo.checkAchievements(email)
                _userRole.value = cloudSyncRepo.getUserRole(email)
                _userKarma.value = cloudSyncRepo.getUserKarma(email)
                _userName.value = cloudSyncRepo.getNickname(email)
                
                val awardIds = cloudSyncRepo.getUserAwards(email)
                _userAwards.value = AVAILABLE_AWARDS.filter { awardIds.contains(it.id) }
                
                val displayedIds = cloudSyncRepo.getDisplayedAwards(email)
                if (displayedIds.isEmpty() && awardIds.isNotEmpty()) {
                    _displayedAwards.value = _userAwards.value.take(7)
                } else {
                    _displayedAwards.value = AVAILABLE_AWARDS.filter { displayedIds.contains(it.id) }
                }
                _isLoadingNickname.value = false
            }
        }
    }

    private val badWords = listOf("хуй", "пизд", "еба", "бля", "хер", "шлюх", "залуп")
    private val urlRegex = Regex(".*[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}.*|.*http.*|.*www.*")

    fun setNickname(nickname: String) {
        val email = auth.currentUser?.email ?: return
        val lower = nickname.lowercase().trim()

        if (lower.length !in 3..20) {
            _nicknameError.value = "Никнейм должен быть от 3 до 20 символов"
            return
        }
        if (urlRegex.matches(lower)) {
            _nicknameError.value = "В никнейме не должно быть ссылок"
            return
        }
        for (word in badWords) {
            if (lower.contains(word)) {
                _nicknameError.value = "Недопустимый никнейм"
                return
            }
        }

        viewModelScope.launch {
            _isLoadingNickname.value = true
            try {
                val isUnique = cloudSyncRepo.checkNicknameUnique(nickname, email)
                if (!isUnique) {
                    _nicknameError.value = "Этот никнейм уже занят"
                    _isLoadingNickname.value = false
                    return@launch
                }

                val success = cloudSyncRepo.setNickname(email, nickname)
                if (success) {
                    _userName.value = nickname
                    _nicknameError.value = null
                } else {
                    _nicknameError.value = "Ошибка сервера. Попробуйте еще раз."
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Неизвестная ошибка"
                if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("Missing or insufficient permissions", ignoreCase = true)) {
                    _nicknameError.value = "Ошибка доступа (Permission Denied). Обновите правила Firestore!"
                } else {
                    _nicknameError.value = "Ошибка: $msg"
                }
            }
            _isLoadingNickname.value = false
        }
    }

    fun grantAllAwardsToSelf() {
        val email = _userEmail.value
        if (email.isNotBlank() && _isOwnProfile.value) {
            viewModelScope.launch {
                cloudSyncRepo.grantAllAwards(email)
                loadProfile(null)
            }
        }
    }

    fun saveDisplayedAwards(awardIds: List<String>) {
        val email = _userEmail.value
        if (email.isNotBlank() && _isOwnProfile.value) {
            viewModelScope.launch {
                cloudSyncRepo.setDisplayedAwards(email, awardIds)
                _displayedAwards.value = AVAILABLE_AWARDS.filter { awardIds.contains(it.id) }
            }
        }
    }

    fun logout() {
        auth.signOut()
        viewModelScope.launch {
            try { auth.signInAnonymously().await() } catch (e: Exception) {}
            _userEmail.value = ""
            _userRole.value = "user"
            _userName.value = null
            _userKarma.value = 0
            _userAwards.value = emptyList()
        }
    }
}
