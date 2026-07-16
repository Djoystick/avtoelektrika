package com.example.autoelectricai.ui.knowledgebase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.DiagnosisRepository
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.sync.CloudSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnowledgeBaseViewModel @Inject constructor(
    private val repository: DiagnosisRepository,
    private val cloudSync: CloudSyncRepository
) : ViewModel() {

    private val _communityDiagnoses = MutableStateFlow<List<DiagnosisEntity>>(emptyList())
    val communityDiagnoses: StateFlow<List<DiagnosisEntity>> = _communityDiagnoses.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserRole = MutableStateFlow("user")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var authStateListener: com.google.firebase.auth.FirebaseAuth.AuthStateListener? = null

    init {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            val email = com.example.autoelectricai.utils.AuthUtils.currentUserEmail
            _currentUserEmail.value = email

            viewModelScope.launch {
                if (email.isNotBlank()) {
                    try {
                        _currentUserRole.value = cloudSync.getUserRole(email)
                    } catch (e: Exception) {
                        _currentUserRole.value = "user"
                    }
                } else {
                    _currentUserRole.value = "user"
                }
            }
        }
        auth.addAuthStateListener(authStateListener!!)

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.allDiagnoses.collect { list ->
                // Энциклопедия теперь показывает ТОЛЬКО проверенные решения сообщества (Глобальная база)
                _communityDiagnoses.value = list.filter { it.isFromCommunity }.sortedByDescending { it.createdAt }
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { com.google.firebase.auth.FirebaseAuth.getInstance().removeAuthStateListener(it) }
    }

    fun voteForSolution(entity: DiagnosisEntity, isLike: Boolean) {
        viewModelScope.launch {
            val cloudId = entity.cloudId ?: return@launch
            val success = cloudSync.vote(cloudId, entity.id, isLike)
            if (success) {
                val oldVote = entity.userVote
                val newVote = if (isLike) "like" else "dislike"
                var newLikes = entity.likes
                var newDislikes = entity.dislikes
                var finalVote: String? = newVote
                
                if (oldVote == newVote) {
                    finalVote = null
                    if (isLike) newLikes -= 1 else newDislikes -= 1
                } else {
                    if (isLike) {
                        newLikes += 1
                        if (oldVote == "dislike") newDislikes -= 1
                    } else {
                        newDislikes += 1
                        if (oldVote == "like") newLikes -= 1
                    }
                }
                
                repository.updateDiagnosis(entity.copy(
                    likes = newLikes,
                    dislikes = newDislikes,
                    userVote = finalVote
                ))
            }
        }
    }

    fun appendAddendum(entity: DiagnosisEntity, text: String) {
        if (text.isBlank()) return
        val cloudId = entity.cloudId ?: return
        viewModelScope.launch {
            val success = cloudSync.appendAddendum(cloudId, text)
            if (success) {
                cloudSync.pullCommunityUpdates()
            }
        }
    }
}
