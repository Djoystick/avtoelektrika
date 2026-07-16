package com.example.autoelectricai.data.search

import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.db.DtcDao
import com.example.autoelectricai.data.db.DtcEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val dtcDao: DtcDao,
    private val diagnosisDao: DiagnosisDao
) {
    companion object {
        private val DTC_PATTERN = Regex("^[PpBbCcUu]\\d{4}$")
        private val DTC_PREFIX_PATTERN = Regex("^[PpBbCcUu]\\d{1,3}$")
    }

    suspend fun search(query: String, brandFilter: String = ""): SearchResult {
        val trimmed = query.trim()
        if (trimmed.length < 2) return SearchResult(emptyList(), emptyList())

        val isFullDtc = DTC_PATTERN.matches(trimmed)
        val isDtcPrefix = DTC_PREFIX_PATTERN.matches(trimmed)

        val dtcResults = mutableListOf<DtcEntity>()
        val diagnosisResults = mutableListOf<DiagnosisEntity>()

        if (isFullDtc) {
            dtcDao.findByCode(trimmed.uppercase())?.let { dtcResults.add(it) }
            diagnosisResults.addAll(diagnosisDao.searchByErrorCode(trimmed.uppercase()))
        } else if (isDtcPrefix) {
            dtcResults.addAll(dtcDao.searchByPrefix(trimmed.uppercase()))
        }

        val expansion = SlangDictionary.normalize(trimmed)
        val ftsQuery = expansion.expandedQuery

        try {
            diagnosisResults.addAll(diagnosisDao.searchFts(ftsQuery, limit = 20))
        } catch (e: Exception) {
            diagnosisResults.addAll(diagnosisDao.searchDiagnoses(trimmed, brandFilter))
        }

        if (!isFullDtc && !isDtcPrefix) {
            try {
                dtcResults.addAll(dtcDao.searchFts(ftsQuery, limit = 10))
            } catch (_: Exception) {}
        }

        val uniqueDiagnoses = diagnosisResults.distinctBy { it.cloudId ?: it.id }
            .sortedByDescending { it.successCount * 2 + it.likes - it.dislikes }
            .take(20)

        return SearchResult(
            dtcResults = dtcResults.distinctBy { it.code }.take(10),
            diagnosisResults = uniqueDiagnoses
        )
    }

    suspend fun quickSearch(query: String): SearchResult {
        val trimmed = query.trim()
        if (trimmed.length < 2) return SearchResult(emptyList(), emptyList())

        val dtcResults = mutableListOf<DtcEntity>()
        val diagnosisResults = mutableListOf<DiagnosisEntity>()

        if (DTC_PATTERN.matches(trimmed)) {
            dtcDao.findByCode(trimmed.uppercase())?.let { dtcResults.add(it) }
        } else if (DTC_PREFIX_PATTERN.matches(trimmed)) {
            dtcResults.addAll(dtcDao.searchByPrefix(trimmed.uppercase()))
        }

        try {
            val expansion = SlangDictionary.normalize(trimmed)
            diagnosisResults.addAll(diagnosisDao.searchFts(expansion.expandedQuery, limit = 5))
        } catch (e: Exception) {
            diagnosisResults.addAll(diagnosisDao.searchDiagnoses(trimmed))
        }

        return SearchResult(
            dtcResults = dtcResults.take(5),
            diagnosisResults = diagnosisResults.take(5)
        )
    }

    data class SearchResult(
        val dtcResults: List<DtcEntity>,
        val diagnosisResults: List<DiagnosisEntity>
    )
}
