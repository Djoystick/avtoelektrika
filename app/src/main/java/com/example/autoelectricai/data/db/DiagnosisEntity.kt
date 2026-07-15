package com.example.autoelectricai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnoses")
data class DiagnosisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carBrand: String = "",
    val carModel: String = "",
    val carYear: String = "",
    val system: String = "",
    val symptoms: String = "",
    val errorCodes: String = "",
    val solution: String = "",
    val source: String = "ai_generated", // "ai_generated", "auto_parsed", "user_saved"
    val successCount: Int = 0,
    val isOfflineReady: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val aiProvider: String = "gemini", // which AI generated this
    
    // Cloud sync fields (added in DB v2)
    val cloudId: String? = null,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val userVote: String? = null, // "like", "dislike", or null
    val isFromCommunity: Boolean = false,
    val authorEmail: String = "", // Added in DB v3
    val authorUsername: String? = null, // Added in DB v4
    
    // AI Verification fields (added in DB v5)
    val aiConfidenceScore: Int? = null,
    val aiConfidenceReason: String? = null
)
