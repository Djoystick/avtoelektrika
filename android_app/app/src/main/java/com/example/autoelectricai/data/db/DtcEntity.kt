package com.example.autoelectricai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "dtc_catalog",
    indices = [
        androidx.room.Index(value = ["code"], name = "idx_dtc_code"),
        androidx.room.Index(value = ["severity"], name = "idx_dtc_severity"),
        androidx.room.Index(value = ["system"], name = "idx_dtc_system")
    ]
)
data class DtcEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "description_ru")
    val descriptionRu: String,

    @ColumnInfo(name = "description_en", defaultValue = "''")
    val descriptionEn: String = "",

    @ColumnInfo(name = "system")
    val system: String,

    @ColumnInfo(name = "severity", defaultValue = "'warning'")
    val severity: String = "warning",

    @ColumnInfo(name = "common_causes", defaultValue = "'[]'")
    val commonCauses: String = "[]",

    @ColumnInfo(name = "common_fixes", defaultValue = "'[]'")
    val commonFixes: String = "[]",

    @ColumnInfo(name = "related_codes", defaultValue = "''")
    val relatedCodes: String = "",

    @ColumnInfo(name = "affected_brands", defaultValue = "'*'")
    val affectedBrands: String = "*",

    @ColumnInfo(name = "is_generic", defaultValue = "1")
    val isGeneric: Boolean = true
)
