package com.example.autoelectricai.ui.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.sync.CloudSyncRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val cloudSyncRepo: CloudSyncRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _pendingSolutions = MutableStateFlow<List<DiagnosisEntity>>(emptyList())
    val pendingSolutions = _pendingSolutions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    // Group solutions by brand for the catalog view
    val groupedSolutions: Map<String, List<DiagnosisEntity>>
        get() = _pendingSolutions.value.groupBy { it.carBrand.ifBlank { "Без марки" } }

    init {
        loadPendingSolutions()
    }

    fun loadPendingSolutions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _pendingSolutions.value = cloudSyncRepo.getPendingSolutions()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveSolution(cloudId: String) {
        viewModelScope.launch {
            cloudSyncRepo.approveSolution(cloudId)
            _pendingSolutions.value = _pendingSolutions.value.filter { it.cloudId != cloudId }
            _actionMessage.value = "✅ Решение одобрено и опубликовано"
        }
    }

    fun rejectSolution(cloudId: String) {
        viewModelScope.launch {
            cloudSyncRepo.rejectSolution(cloudId)
            _pendingSolutions.value = _pendingSolutions.value.filter { it.cloudId != cloudId }
            _actionMessage.value = "❌ Решение отклонено"
        }
    }

    fun approveWithEdit(cloudId: String, editedSolution: String, editedSymptoms: String) {
        viewModelScope.launch {
            // Update the solution text before approving
            val item = _pendingSolutions.value.find { it.cloudId == cloudId } ?: return@launch
            val updated = item.copy(solution = editedSolution, symptoms = editedSymptoms)
            cloudSyncRepo.updateAndApproveSolution(cloudId, updated)
            _pendingSolutions.value = _pendingSolutions.value.filter { it.cloudId != cloudId }
            _actionMessage.value = "✏️ Решение отредактировано и одобрено"
        }
    }

    fun clearMessage() {
        _actionMessage.value = null
    }
}
