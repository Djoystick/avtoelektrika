package com.example.autoelectricai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_cache",
    indices = [
        androidx.room.Index(value = ["diagnosis_id"], name = "idx_offline_cache_diagnosis"),
        androidx.room.Index(value = ["last_accessed_at"], name = "idx_offline_cache_accessed")
    ]
)
data class OfflineCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "diagnosis_id")
    val diagnosisId: Long,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long,

    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: Long,

    @ColumnInfo(name = "access_count", defaultValue = "1")
    val accessCount: Int = 1,

    @ColumnInfo(name = "size_bytes", defaultValue = "0")
    val sizeBytes: Long = 0
)
