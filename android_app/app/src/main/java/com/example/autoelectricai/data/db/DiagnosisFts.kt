package com.example.autoelectricai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = DiagnosisEntity::class)
@Entity(tableName = "diagnoses_fts")
data class DiagnosisFts(
    @ColumnInfo(name = "carBrand")
    val carBrand: String,

    @ColumnInfo(name = "carModel")
    val carModel: String,

    @ColumnInfo(name = "system")
    val system: String,

    @ColumnInfo(name = "symptoms")
    val symptoms: String,

    @ColumnInfo(name = "errorCodes")
    val errorCodes: String,

    @ColumnInfo(name = "solution")
    val solution: String,

    @ColumnInfo(name = "encyclopediaPlatform")
    val encyclopediaPlatform: String = "",

    @ColumnInfo(name = "encyclopediaSystem")
    val encyclopediaSystem: String = "",

    @ColumnInfo(name = "encyclopediaSubsystem")
    val encyclopediaSubsystem: String = ""
)
