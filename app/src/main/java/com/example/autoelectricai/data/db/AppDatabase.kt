package com.example.autoelectricai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DiagnosisEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diagnosisDao(): DiagnosisDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN cloudId TEXT")
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN likes INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN dislikes INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN userVote TEXT")
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN isFromCommunity INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN authorEmail TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN authorUsername TEXT")
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN aiConfidenceScore INTEGER")
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN aiConfidenceReason TEXT")
            }
        }
    }
}
