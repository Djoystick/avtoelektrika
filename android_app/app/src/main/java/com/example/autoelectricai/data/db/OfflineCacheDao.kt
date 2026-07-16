package com.example.autoelectricai.data.db

import androidx.room.*

@Dao
interface OfflineCacheDao {

    @Query("""
        INSERT OR REPLACE INTO offline_cache 
        (diagnosis_id, cached_at, last_accessed_at, access_count, size_bytes)
        VALUES (
            :diagnosisId,
            COALESCE((SELECT cached_at FROM offline_cache WHERE diagnosis_id = :diagnosisId), :now),
            :now,
            COALESCE((SELECT access_count FROM offline_cache WHERE diagnosis_id = :diagnosisId), 0) + 1,
            :sizeBytes
        )
    """)
    suspend fun upsert(diagnosisId: Long, now: Long = System.currentTimeMillis(), sizeBytes: Long = 0)

    @Query("SELECT diagnosis_id FROM offline_cache ORDER BY last_accessed_at ASC")
    suspend fun getCachedIds(): List<Long>

    @Query("SELECT COUNT(*) FROM offline_cache")
    suspend fun getCachedCount(): Int

    @Query("DELETE FROM offline_cache WHERE diagnosis_id = :diagnosisId")
    suspend fun evict(diagnosisId: Long)

    @Query("""
        DELETE FROM offline_cache 
        WHERE id IN (
            SELECT id FROM offline_cache 
            ORDER BY last_accessed_at ASC 
            LIMIT :count
        )
    """)
    suspend fun evictOldest(count: Int)

    @Query("DELETE FROM offline_cache")
    suspend fun clearAll()
}
