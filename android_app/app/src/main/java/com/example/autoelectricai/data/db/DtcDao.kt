package com.example.autoelectricai.data.db

import androidx.room.*

@Dao
interface DtcDao {

    @Query("SELECT * FROM dtc_catalog WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): DtcEntity?

    @Query("SELECT * FROM dtc_catalog WHERE code LIKE :prefix || '%' ORDER BY code LIMIT 20")
    suspend fun searchByPrefix(prefix: String): List<DtcEntity>

    @Query("""
        SELECT dtc_catalog.* FROM dtc_catalog
        JOIN dtc_catalog_fts ON dtc_catalog.rowid = dtc_catalog_fts.rowid
        WHERE dtc_catalog_fts MATCH :query
        LIMIT :limit
    """)
    suspend fun searchFts(query: String, limit: Int = 20): List<DtcEntity>

    @Query("SELECT * FROM dtc_catalog WHERE system = :system ORDER BY code LIMIT :limit")
    suspend fun findBySystem(system: String, limit: Int = 50): List<DtcEntity>

    @Query("SELECT * FROM dtc_catalog WHERE severity = 'critical' ORDER BY RANDOM() LIMIT :limit")
    suspend fun getCriticalCodes(limit: Int = 5): List<DtcEntity>

    @Query("SELECT * FROM dtc_catalog ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomCodes(limit: Int = 10): List<DtcEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(catalog: List<DtcEntity>)

    @Query("SELECT COUNT(*) FROM dtc_catalog")
    suspend fun getCount(): Int

    @Query("DELETE FROM dtc_catalog")
    suspend fun deleteAll()
}
