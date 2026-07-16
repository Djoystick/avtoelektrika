package com.example.autoelectricai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DiagnosisEntity::class,
        DtcEntity::class,
        OfflineCacheEntity::class,
        DiagnosisFts::class,
        DtcFts::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diagnosisDao(): DiagnosisDao
    abstract fun dtcDao(): DtcDao
    abstract fun offlineCacheDao(): OfflineCacheDao

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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN encyclopediaPlatform TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN encyclopediaSystem TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE diagnoses ADD COLUMN encyclopediaSubsystem TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS dtc_catalog (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        code TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description_ru TEXT NOT NULL,
                        description_en TEXT NOT NULL DEFAULT '',
                        system TEXT NOT NULL,
                        severity TEXT NOT NULL DEFAULT 'warning',
                        common_causes TEXT NOT NULL DEFAULT '[]',
                        common_fixes TEXT NOT NULL DEFAULT '[]',
                        related_codes TEXT NOT NULL DEFAULT '',
                        affected_brands TEXT NOT NULL DEFAULT '*',
                        is_generic INTEGER NOT NULL DEFAULT 1
                    )
                """)
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS dtc_catalog_fts 
                    USING fts4(code, description_ru, description_en, system, 
                               content='dtc_catalog')
                """)
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS diagnoses_fts 
                    USING fts4(carBrand, carModel, system, symptoms, errorCodes, solution,
                               encyclopediaPlatform, encyclopediaSystem, encyclopediaSubsystem,
                               content='diagnoses')
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS offline_cache (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        diagnosis_id INTEGER NOT NULL,
                        cached_at INTEGER NOT NULL,
                        last_accessed_at INTEGER NOT NULL,
                        access_count INTEGER NOT NULL DEFAULT 1,
                        size_bytes INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS diagnoses_ai AFTER INSERT ON diagnoses BEGIN
                        INSERT INTO diagnoses_fts(rowid, carBrand, carModel, system, symptoms, errorCodes, solution,
                            encyclopediaPlatform, encyclopediaSystem, encyclopediaSubsystem)
                        VALUES (new.id, new.carBrand, new.carModel, new.system, new.symptoms, new.errorCodes, new.solution,
                            new.encyclopediaPlatform, new.encyclopediaSystem, new.encyclopediaSubsystem);
                    END
                """)
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS diagnoses_au AFTER UPDATE ON diagnoses BEGIN
                        DELETE FROM diagnoses_fts WHERE rowid = old.id;
                        INSERT INTO diagnoses_fts(rowid, carBrand, carModel, system, symptoms, errorCodes, solution,
                            encyclopediaPlatform, encyclopediaSystem, encyclopediaSubsystem)
                        VALUES (new.id, new.carBrand, new.carModel, new.system, new.symptoms, new.errorCodes, new.solution,
                            new.encyclopediaPlatform, new.encyclopediaSystem, new.encyclopediaSubsystem);
                    END
                """)
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS diagnoses_ad AFTER DELETE ON diagnoses BEGIN
                        DELETE FROM diagnoses_fts WHERE rowid = old.id;
                    END
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_dtc_code ON dtc_catalog(code)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_dtc_system ON dtc_catalog(system)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_dtc_severity ON dtc_catalog(severity)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_offline_cache_diagnosis ON offline_cache(diagnosis_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_offline_cache_accessed ON offline_cache(last_accessed_at)")
            }
        }
    }
}
