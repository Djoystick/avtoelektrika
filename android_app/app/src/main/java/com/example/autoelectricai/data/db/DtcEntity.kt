package com.example.autoelectricai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dtc_catalog")
data class DtcEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "description_ru")
    val descriptionRu: String,

    @ColumnInfo(name = "description_en")
    val descriptionEn: String = "",

    @ColumnInfo(name = "system")
    val system: String,

    @ColumnInfo(name = "severity")
    val severity: String = "warning",

    @ColumnInfo(name = "common_causes")
    val commonCauses: String = "[]",

    @ColumnInfo(name = "common_fixes")
    val commonFixes: String = "[]",

    @ColumnInfo(name = "related_codes")
    val relatedCodes: String = "",

    @ColumnInfo(name = "affected_brands")
    val affectedBrands: String = "*",

    @ColumnInfo(name = "is_generic")
    val isGeneric: Boolean = true
)
