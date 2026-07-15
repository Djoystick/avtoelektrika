package com.example.autoelectricai.data

import com.example.autoelectricai.data.ai.AiResult
import com.example.autoelectricai.data.ai.AiService
import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.db.RecentCar
import com.example.autoelectricai.data.encyclopedia.EncyclopediaCatalog
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

        // Look up encyclopedia catalog context for this brand to guide AI categorization
        val encBrand = EncyclopediaCatalog.brands.find {
            it.displayName.equals(carBrand, ignoreCase = true) ||
            it.shortName.equals(carBrand, ignoreCase = true) ||
            it.id.equals(carBrand, ignoreCase = true)
        }
        val catalogPlatforms = encBrand?.platforms?.map { it.displayName } ?: emptyList()
        val catalogSystems = encBrand?.platforms?.flatMap { p -> p.systems.map { it.displayName } }?.distinct() ?: emptyList()

        val aiResult = aiService.generateDiagnosis(
            carBrand, carModel, carYear, system, symptoms, errorCodes,
            geminiKey, geminiProxyUrl, openAiKey, preferredAi,
            catalogPlatforms, catalogSystems
        )

        return when (aiResult) {
            is AiResult.Success -> {
                // Extract encyclopedia categorization fields from AI JSON
                val encPlatform = extractJsonStringField(aiResult.jsonText, "encyclopediaPlatform")
                val encSystem = extractJsonStringField(aiResult.jsonText, "encyclopediaSystem")
                val encSubsystem = extractJsonStringField(aiResult.jsonText, "encyclopediaSubsystem")

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
                    authorEmail = com.example.autoelectricai.utils.AuthUtils.currentUserEmail,
                    encyclopediaPlatform = encPlatform,
                    encyclopediaSystem = encSystem,
                    encyclopediaSubsystem = encSubsystem
                )
                val id = dao.insert(entity)
                DiagnosisResult.AiHit(entity.copy(id = id), aiResult.provider)
            }
            is AiResult.Error -> DiagnosisResult.Failure(aiResult.message)
        }
    }

    /** Safely extracts a top-level string field from a JSON string without full parsing */
    private fun extractJsonStringField(json: String, fieldName: String): String {
        return try {
            val regex = Regex("\"$fieldName\"\\s*:\\s*\"(.*?)\"", RegexOption.DOT_MATCHES_ALL)
            regex.find(json)?.groupValues?.getOrNull(1)?.trim() ?: ""
        } catch (e: Exception) {
            ""
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
