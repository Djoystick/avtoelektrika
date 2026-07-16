package com.example.autoelectricai.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class RecentCar(
    val brand: String,
    val model: String,
    val year: String
)

@Dao
interface DiagnosisDao {

    @Query("SELECT * FROM diagnoses ORDER BY createdAt DESC")
    fun getAllDiagnoses(): Flow<List<DiagnosisEntity>>

    @Query("SELECT * FROM diagnoses WHERE isOfflineReady = 1 ORDER BY successCount DESC")
    fun getOfflineDiagnoses(): Flow<List<DiagnosisEntity>>

    @Query("SELECT COUNT(*) FROM diagnoses")
    suspend fun getSavedCount(): Int

    @Query("""
        SELECT * FROM diagnoses 
        WHERE (symptoms LIKE '%' || :query || '%' 
            OR errorCodes LIKE '%' || :query || '%'
            OR system LIKE '%' || :query || '%')
        AND (:brand = '' OR carBrand LIKE '%' || :brand || '%')
        ORDER BY (successCount * 2 + likes - dislikes) DESC, createdAt DESC
        LIMIT 10
    """)
    suspend fun searchDiagnoses(query: String, brand: String = ""): List<DiagnosisEntity>

    @Query("""
        SELECT * FROM diagnoses
        WHERE carBrand LIKE '%' || :brand || '%'
        AND symptoms LIKE '%' || :symptom || '%'
        ORDER BY (successCount * 2 + likes - dislikes) DESC
        LIMIT 5
    """)
    suspend fun findSimilar(brand: String, symptom: String): List<DiagnosisEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diagnosis: DiagnosisEntity): Long

    @Update
    suspend fun update(diagnosis: DiagnosisEntity)

    @Delete
    suspend fun delete(diagnosis: DiagnosisEntity)

    @Query("UPDATE diagnoses SET successCount = successCount + 1, isOfflineReady = 1 WHERE id = :id")
    suspend fun markAsSuccessful(id: Long)

    @Query("UPDATE diagnoses SET successCount = successCount + 1, isOfflineReady = 1, solution = :newSolution WHERE id = :id")
    suspend fun markAsSuccessfulWithContent(id: Long, newSolution: String)

    @Query("SELECT DISTINCT carBrand FROM diagnoses WHERE carBrand != '' ORDER BY carBrand")
    suspend fun getAllBrands(): List<String>

    @Query("SELECT COUNT(*) FROM diagnoses WHERE isOfflineReady = 1")
    suspend fun getOfflineCount(): Int

    @Query("SELECT carBrand as brand, carModel as model, carYear as year FROM diagnoses WHERE carBrand != '' GROUP BY carBrand, carModel, carYear ORDER BY MAX(createdAt) DESC LIMIT 5")
    fun getRecentCars(): Flow<List<RecentCar>>

    @Query("SELECT * FROM diagnoses WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getByCloudId(cloudId: String): DiagnosisEntity?

    @Query("SELECT * FROM diagnoses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DiagnosisEntity?

    @Query("""
        SELECT diagnoses.* FROM diagnoses
        JOIN diagnoses_fts ON diagnoses.rowid = diagnoses_fts.rowid
        WHERE diagnoses_fts MATCH :query
        ORDER BY (successCount * 2 + likes - dislikes) DESC, createdAt DESC
        LIMIT :limit
    """)
    suspend fun searchFts(query: String, limit: Int = 20): List<DiagnosisEntity>

    @Query("""
        SELECT * FROM diagnoses 
        WHERE errorCodes LIKE '%' || :errorCode || '%'
        ORDER BY (successCount * 2 + likes - dislikes) DESC
        LIMIT :limit
    """)
    suspend fun searchByErrorCode(errorCode: String, limit: Int = 20): List<DiagnosisEntity>

    @Query("SELECT * FROM diagnoses WHERE successCount > 0 ORDER BY successCount DESC LIMIT :limit")
    suspend fun getPopularDiagnoses(limit: Int = 10): List<DiagnosisEntity>

    @Query("SELECT * FROM diagnoses ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentDiagnoses(limit: Int = 10): List<DiagnosisEntity>

    @Query("SELECT * FROM diagnoses WHERE isOfflineReady = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentOffline(limit: Int = 50): List<DiagnosisEntity>
}
