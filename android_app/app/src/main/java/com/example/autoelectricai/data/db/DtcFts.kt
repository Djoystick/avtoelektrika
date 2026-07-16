package com.example.autoelectricai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = DtcEntity::class)
@Entity(tableName = "dtc_catalog_fts")
data class DtcFts(
    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "description_ru")
    val descriptionRu: String,

    @ColumnInfo(name = "description_en")
    val descriptionEn: String = "",

    @ColumnInfo(name = "system")
    val system: String
)
