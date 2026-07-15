package com.example.autoelectricai.data

import com.example.autoelectricai.data.ai.AiResult
import com.example.autoelectricai.data.ai.AiService
import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.db.RecentCar
import com.example.autoelectricai.data.prefs.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosisRepository @Inject constructor(
    private val dao: DiagnosisDao,
    private val aiService: AiService,
    private val settings: SettingsRepository,
    private val auth: FirebaseAuth
) {
    val allDiagnoses: Flow<List<DiagnosisEntity>> = dao.getAllDiagnoses()
    val offlineDiagnoses: Flow<List<DiagnosisEntity>> = dao.getOfflineDiagnoses()
    val recentCars: Flow<List<RecentCar>> = dao.getRecentCars()

    suspend fun searchLocal(query: String, brand: String = ""): List<DiagnosisEntity> =
        dao.searchDiagnoses(query, brand)

    suspend fun findSimilar(brand: String, symptom: String): List<DiagnosisEntity> =
        dao.findSimilar(brand, symptom)

    suspend fun getAllBrands(): List<String> = dao.getAllBrands()

    suspend fun getOfflineCount(): Int = dao.getOfflineCount()

    suspend fun generateAndSave(
        carBrand: String,
        carModel: String,
        carYear: String,
        system: String,
        symptoms: String,
        errorCodes: String
    ): DiagnosisResult {
        // First try local search
        val localQuery = buildString {
            if (symptoms.isNotBlank()) append(symptoms)
            if (errorCodes.isNotBlank()) append(" $errorCodes")
        }
        val localResults = dao.searchDiagnoses(localQuery, carBrand)
        if (localResults.isNotEmpty()) {
            return DiagnosisResult.LocalHit(localResults.first())
        }

        // No local hit — go to AI
        val geminiKey = settings.geminiApiKey.first()
        val geminiProxyUrl = settings.geminiProxyUrl.first()
        val openAiKey = settings.openAiApiKey.first()
        val preferredAi = settings.preferredAi.first()

        val aiResult = aiService.generateDiagnosis(
            carBrand, carModel, carYear, system, symptoms, errorCodes,
            geminiKey, geminiProxyUrl, openAiKey, preferredAi
        )

        return when (aiResult) {
            is AiResult.Success -> {
                val entity = DiagnosisEntity(
                    carBrand = carBrand,
                    carModel = carModel,
                    carYear = carYear,
                    system = system,
                    symptoms = symptoms,
                    errorCodes = errorCodes,
                    solution = aiResult.jsonText,
                    source = "ai_generated",
                    aiProvider = aiResult.provider,
                    isOfflineReady = false,
                    authorEmail = auth.currentUser?.email ?: ""
                )
                val id = dao.insert(entity)
                DiagnosisResult.AiHit(entity.copy(id = id), aiResult.provider)
            }
            is AiResult.Error -> DiagnosisResult.Failure(aiResult.message)
        }
    }

    suspend fun markAsSuccessful(id: Long) = dao.markAsSuccessful(id)

    suspend fun markAsSuccessfulWithContent(id: Long, newSolution: String) = dao.markAsSuccessfulWithContent(id, newSolution)

    suspend fun deleteDiagnosis(entity: DiagnosisEntity) = dao.delete(entity)

    suspend fun updateDiagnosis(entity: DiagnosisEntity) = dao.update(entity)
}

sealed class DiagnosisResult {
    data class LocalHit(val entity: DiagnosisEntity) : DiagnosisResult()
    data class AiHit(val entity: DiagnosisEntity, val provider: String) : DiagnosisResult()
    data class Failure(val message: String) : DiagnosisResult()
}
