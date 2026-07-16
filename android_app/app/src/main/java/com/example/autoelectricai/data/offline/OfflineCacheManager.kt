package com.example.autoelectricai.data.offline

import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.OfflineCacheDao
import com.example.autoelectricai.utils.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineCacheManager @Inject constructor(
    private val cacheDao: OfflineCacheDao,
    private val diagnosisDao: DiagnosisDao
) {
    companion object {
        private const val TAG = "OfflineCacheManager"
        private const val MAX_CACHE_SIZE = 50
    }

    suspend fun trackAccess(diagnosisId: Long) {
        val entity = diagnosisDao.getById(diagnosisId) ?: return
        val sizeBytes = entity.solution.toByteArray().size.toLong()
        cacheDao.upsert(diagnosisId, sizeBytes = sizeBytes)
        enforceMaxSize()
    }

    suspend fun markOfflineReady(diagnosisId: Long) {
        diagnosisDao.markAsSuccessful(diagnosisId)
        val entity = diagnosisDao.getById(diagnosisId) ?: return
        val sizeBytes = entity.solution.toByteArray().size.toLong()
        cacheDao.upsert(diagnosisId, sizeBytes = sizeBytes)
        enforceMaxSize()
    }

    suspend fun getCachedDiagnoses() = diagnosisDao.getRecentOffline(MAX_CACHE_SIZE)

    suspend fun getCachedCount() = cacheDao.getCachedCount()

    suspend fun clearCache() {
        cacheDao.clearAll()
        AppLogger.i(TAG, "Offline cache cleared")
    }

    private suspend fun enforceMaxSize() {
        val count = cacheDao.getCachedCount()
        if (count > MAX_CACHE_SIZE) {
            val excess = count - MAX_CACHE_SIZE
            cacheDao.evictOldest(excess)
            AppLogger.i(TAG, "Evicted $excess old cache entries")
        }
    }
}
