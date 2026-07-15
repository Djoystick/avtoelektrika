package com.example.autoelectricai.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.DiagnosisRepository
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: DiagnosisRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _bookmarks = MutableStateFlow<List<DiagnosisEntity>>(emptyList())
    val bookmarks: StateFlow<List<DiagnosisEntity>> = _bookmarks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val email = auth.currentUser?.email ?: ""
        
        viewModelScope.launch {
            repository.allDiagnoses.collect { list ->
                val myBookmarks = list.filter { e ->
                    if (email.isNotBlank()) {
                        e.authorEmail == email || (!e.isFromCommunity && e.authorEmail.isBlank())
                    } else {
                        !e.isFromCommunity && e.authorEmail.isBlank()
                    }
                }.sortedByDescending { it.createdAt }
                
                _bookmarks.value = myBookmarks
                _isLoading.value = false
            }
        }
    }

    fun deleteBookmark(entity: DiagnosisEntity) {
        viewModelScope.launch { repository.deleteDiagnosis(entity) }
    }
}
