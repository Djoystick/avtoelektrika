package com.example.autoelectricai.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.update.AppUpdateManager
import com.example.autoelectricai.data.update.DownloadState
import com.example.autoelectricai.data.update.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _hasChecked = MutableStateFlow(false)
    val hasChecked: StateFlow<Boolean> = _hasChecked.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    init {
        // Automatic check removed per user request for manual check only
    }

    fun checkForUpdates() {
        if (_isChecking.value) return
        _isChecking.value = true
        _hasChecked.value = false
        viewModelScope.launch {
            val info = appUpdateManager.checkForUpdate()
            if (info != null) {
                _updateInfo.value = info
            } else {
                _updateInfo.value = null
            }
            _isChecking.value = false
            _hasChecked.value = true
        }
    }

    private var downloadJob: kotlinx.coroutines.Job? = null

    fun startDownload() {
        val info = _updateInfo.value ?: return
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            appUpdateManager.downloadUpdate(info.downloadUrl, "AutoElectricAI_${info.versionName}.apk").collect { state ->
                _downloadState.value = state
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _downloadState.value = DownloadState.Idle
    }

    fun dismissUpdate() {
        cancelDownload()
        _updateInfo.value = null
    }
}
