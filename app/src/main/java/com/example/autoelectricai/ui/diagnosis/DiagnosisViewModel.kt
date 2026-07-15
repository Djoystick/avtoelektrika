package com.example.autoelectricai.ui.diagnosis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.DiagnosisRepository
import com.example.autoelectricai.data.DiagnosisResult
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.db.RecentCar
import com.example.autoelectricai.data.sync.CloudSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject
import com.example.autoelectricai.data.CarData
import com.example.autoelectricai.utils.AppLogger
import com.google.gson.Gson
import com.example.autoelectricai.data.ai.DiagnosisResponse

enum class DiagnosisStep {
    CAR, SYSTEM, SYMPTOMS
}

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    private val repository: DiagnosisRepository,
    private val cloudSync: CloudSyncRepository
) : ViewModel() {

    // Form fields
    val carBrand = MutableStateFlow("")
    val carModel = MutableStateFlow("")
    val carYear = MutableStateFlow("")
    val selectedSystem = MutableStateFlow("")
    val symptoms = MutableStateFlow("")
    val errorCodes = MutableStateFlow("")

    // UI State
    val currentStep = MutableStateFlow(DiagnosisStep.CAR)

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    fun nextStep() {
        when (currentStep.value) {
            DiagnosisStep.CAR -> currentStep.value = DiagnosisStep.SYSTEM
            DiagnosisStep.SYSTEM -> currentStep.value = DiagnosisStep.SYMPTOMS
            DiagnosisStep.SYMPTOMS -> {}
        }
    }

    fun prevStep(): Boolean {
        return when (currentStep.value) {
            DiagnosisStep.SYMPTOMS -> {
                currentStep.value = DiagnosisStep.SYSTEM
                true
            }
            DiagnosisStep.SYSTEM -> {
                currentStep.value = DiagnosisStep.CAR
                true
            }
            DiagnosisStep.CAR -> {
                if (uiState.value is DiagnosisUiState.Success || uiState.value is DiagnosisUiState.Error) {
                    reset()
                    true
                } else {
                    false
                }
            }
        }
    }

    // Local search suggestions
    private val _suggestions = MutableStateFlow<List<DiagnosisEntity>>(emptyList())
    val suggestions: StateFlow<List<DiagnosisEntity>> = _suggestions.asStateFlow()

    // Saved brands for autocomplete
    private val _dbBrands = MutableStateFlow<List<String>>(emptyList())
    
    val brands: StateFlow<List<String>> = _dbBrands.map { dbList ->
        (CarData.brands + dbList).distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CarData.brands)

    val recentCars: StateFlow<List<RecentCar>> = repository.recentCars.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableModels: StateFlow<List<String>> = carBrand.map { brand ->
        CarData.getModels(brand)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val availableYears: StateFlow<List<String>> = combine(carBrand, carModel) { brand, model ->
        if (brand.isNotBlank() && model.isNotBlank()) {
            CarData.getYearsForModel(brand, model)
        } else {
            CarData.defaultYears
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CarData.defaultYears)

    init {
        loadBrands()
        // При запуске Главной страницы сразу подтягиваем обновы (и подхватываем аккаунт в CloudSyncRepo)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                cloudSync.pullCommunityUpdates()
            } catch (e: Exception) {
                // Игнорируем ошибки при фоновой синхронизации
            }
        }
    }

    private fun loadBrands() {
        viewModelScope.launch {
            _dbBrands.value = repository.getAllBrands()
        }
    }
    
    fun selectRecentCar(car: RecentCar) {
        carBrand.value = car.brand
        carModel.value = car.model
        carYear.value = car.year
        nextStep()
    }

    private var searchJob: Job? = null

    fun onSymptomsChanged(value: String) {
        symptoms.value = value
        searchJob?.cancel()
        if (value.length > 3) {
            searchJob = viewModelScope.launch {
                delay(300)
                _suggestions.value = repository.searchLocal(value, carBrand.value)
            }
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun diagnose() {
        if (symptoms.value.isBlank() && errorCodes.value.isBlank()) {
            _uiState.value = DiagnosisUiState.Error("Введите симптомы или коды ошибок")
            return
        }
        _uiState.value = DiagnosisUiState.Loading
        _suggestions.value = emptyList()
        AppLogger.i("DiagnosisViewModel", "Starting diagnosis for: ${carBrand.value} ${carModel.value}")

        viewModelScope.launch {
            val result = repository.generateAndSave(
                carBrand = carBrand.value.trim(),
                carModel = carModel.value.trim(),
                carYear = carYear.value.trim(),
                system = selectedSystem.value,
                symptoms = symptoms.value.trim(),
                errorCodes = errorCodes.value.trim()
            )
            
            AppLogger.i("DiagnosisViewModel", "Diagnosis result: ${result::class.simpleName}")
            
            _uiState.value = when (result) {
                is DiagnosisResult.LocalHit -> DiagnosisUiState.Success(
                    result.entity, isLocal = true
                )
                is DiagnosisResult.AiHit -> DiagnosisUiState.Success(
                    result.entity, isLocal = false, provider = result.provider
                )
                is DiagnosisResult.Failure -> {
                    AppLogger.e("DiagnosisViewModel", "Failure: ${result.message}")
                    DiagnosisUiState.Error(result.message)
                }
            }
        }
    }

    fun markAsSuccessful(id: Long, selectedIndices: Set<Int> = emptySet()) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is DiagnosisUiState.Success && current.entity.id == id) {
                if (selectedIndices.isNotEmpty()) {
                    try {
                        val gson = Gson()
                        val response = gson.fromJson(current.entity.solution, DiagnosisResponse::class.java)
                        if (response != null && response.solutions.isNotEmpty()) {
                            val filteredSolutions = response.solutions.filterIndexed { index, _ -> selectedIndices.contains(index) }
                            val newResponse = response.copy(solutions = filteredSolutions)
                            val newJson = gson.toJson(newResponse)
                            repository.markAsSuccessfulWithContent(id, newJson)
                            _uiState.value = current.copy(
                                entity = current.entity.copy(solution = newJson),
                                isSaved = true
                            )
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                cloudSync.pushSolution(current.entity.copy(solution = newJson))
                            }
                        } else {
                            repository.markAsSuccessful(id)
                            _uiState.value = current.copy(isSaved = true)
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                cloudSync.pushSolution(current.entity)
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.e("DiagnosisViewModel", "Error filtering JSON solutions", e)
                        repository.markAsSuccessful(id)
                        _uiState.value = current.copy(isSaved = true)
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            cloudSync.pushSolution(current.entity)
                        }
                    }
                } else {
                    repository.markAsSuccessful(id)
                    _uiState.value = current.copy(isSaved = true)
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        cloudSync.pushSolution(current.entity)
                    }
                }
            } else {
                repository.markAsSuccessful(id)
            }
        }
    }

    fun voteForSolution(cloudId: String, entityId: Long, isLike: Boolean) {
        viewModelScope.launch {
            cloudSync.vote(cloudId, entityId, isLike)
            // Обновляем UI локально
            val current = _uiState.value
            if (current is DiagnosisUiState.Success && current.entity.id == entityId) {
                val e = current.entity
                _uiState.value = current.copy(
                    entity = e.copy(
                        likes = if (isLike) e.likes + 1 else e.likes,
                        dislikes = if (!isLike) e.dislikes + 1 else e.dislikes,
                        userVote = if (isLike) "like" else "dislike"
                    )
                )
            }
        }
    }

    fun selectSuggestion(entity: DiagnosisEntity) {
        currentStep.value = DiagnosisStep.SYMPTOMS
        _uiState.value = DiagnosisUiState.Success(entity, isLocal = true)
        _suggestions.value = emptyList()
    }

    fun reset() {
        _uiState.value = DiagnosisUiState.Idle
        _suggestions.value = emptyList()
        clearForm()
        viewModelScope.launch {
            kotlinx.coroutines.delay(50)
            currentStep.value = DiagnosisStep.CAR
        }
    }

    fun clearForm() {
        carBrand.value = ""
        carModel.value = ""
        carYear.value = ""
        selectedSystem.value = ""
        symptoms.value = ""
        errorCodes.value = ""
        currentStep.value = DiagnosisStep.CAR
    }
}

sealed class DiagnosisUiState {
    object Idle : DiagnosisUiState()
    object Loading : DiagnosisUiState()
    data class Success(
        val entity: DiagnosisEntity,
        val isLocal: Boolean,
        val provider: String = "",
        val isSaved: Boolean = false
    ) : DiagnosisUiState()
    data class Error(val message: String) : DiagnosisUiState()
}
