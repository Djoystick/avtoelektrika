package com.example.autoelectricai.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.sync.CloudSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val cloudSyncRepo: CloudSyncRepository
) : ViewModel() {

    private val _topExperts = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val topExperts = _topExperts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _topExperts.value = cloudSyncRepo.getTopExperts()
            _isLoading.value = false
        }
    }
}
