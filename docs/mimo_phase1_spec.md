# Фаза 1 (Quick Wins) — Техническое задание и архитектурные чертежи

**Версия документа:** 1.0  
**Проект:** AutoElectric AI (`com.example.autoelectricai`)  
**Стек:** Kotlin, Jetpack Compose, Room, Hilt, WorkManager, Firebase  
**Текущая версия БД:** v6 (`autoelectric.db`)  

---

## Содержание

1. [Обзор изменений](#1-обзор-изменений)
2. [Архитектура Global Search](#2-архитектура-global-search)
3. [Quick Cards на главном экране](#3-quick-cards)
4. [Typography & Styling](#4-typography--styling)
5. [Offline-кэш (50 элементов)](#5-offline-кэш)
6. [Миграция БД](#6-миграция-бд)
7. [Список файлов для создания/изменения](#7-список-файлов)

---

## 1. Обзор изменений

### Файлы для изменения (существующие):

| Файл | Изменение |
|---|---|
| `AppDatabase.kt` | Версия 6→7, добавить `DtcEntity` + `OfflineCacheEntity`, FTS4 virtual table |
| `DiagnosisDao.kt` | Добавить методы поиска по FTS4, DTC-запросы, offline-кэш |
| `AppModule.kt` | Provides для нового DAO (`DtcDao`, `OfflineCacheDao`) |
| `MainScreen.kt` | Добавить 4-й таб «Поиск», добавить Quick Cards на экран Diagnosis |
| `Type.kt` | Полная переработка typography (bodyLarge=16sp, заголовки≥20sp) |
| `DiagnosisRepository.kt` | Интегрировать поиск по DTC-каталогу, offline-логику |
| `DiagnosisViewModel.kt` | Добавить Quick Cards, offline-first логику |
| `DiagnosisScreen.kt` | Quick Cards above wizard |
| `build.gradle.kts` (app) | Добавить依赖 `androidx.room:room-ktx` (уже есть), `work-runtime-ktx` (уже есть) |

### Файлы для создания (новые):

| Файл | Назначение |
|---|---|
| `data/db/DtcEntity.kt` | Room Entity для OBD2 DTC-каталога |
| `data/db/DtcDao.kt` | DAO с FTS4-поиском по DTC |
| `data/db/OfflineCacheEntity.kt` | Room Entity для offline-кэша |
| `data/db/OfflineCacheDao.kt` | DAO для управления кэшем (LRU) |
| `data/db/DtcCatalog.kt` | Импорт начального набора DTC-кодов |
| `data/search/SearchRepository.kt` | Репозиторий: unified search (DTC + симптомы + сленг) |
| `data/search/SlangDictionary.kt` | Словарь сленга → нормализация |
| `data/offline/OfflineCacheWorker.kt` | WorkManager worker для background-кэширования |
| `data/offline/OfflineCacheManager.kt` | Менеджер: cache eviction, sync status |
| `ui/search/SearchScreen.kt` | Экран глобального поиска |
| `ui/search/SearchViewModel.kt` | ViewModel для поиска |
| `ui/search/DtcDetailScreen.kt` | Экран карточки DTC-кода |
| `ui/search/SearchComponents.kt` | Переиспользуемые UI-компоненты (SuggestionChip, SearchResultCard) |
| `ui/main/QuickCards.kt` | Компонент «Популярное сегодня» / «Недавно добавлено» |

---

## 2. Архитектура Global Search

### 2.1 Стратегия поиска (offline-first, без Algolia)

Поиск работает **полностью локально** через Room FTS4 (Full-Text Search). Это решение chosen вместо Algolia/ElasticSearch, потому что:
- Нет дополнительных серверных затрат
- Работает оффлайн
- FTS4 достаточно быстр для объёма до 50,000 записей
- Простая интеграция с существующим Room-стеком

**Архитектура поиска:**

```
Пользователь вводит запрос
        ↓
SlangDictionary.normalize(query)  ← "не крутит стартер" → "starter.no-crank"
        ↓
┌─────────────────────────────────────────────┐
│ Приоритет 1: DTC-код (regex P/B/C/U + 4цифры) │ → DtcDao.searchByCode()
│ Приоритет 2: FTS4 DiagnosisEntity              │ → DiagnosisDao.searchFts()
│ Приоритет 3: Slang-expanded query              │ → DiagnosisDao.searchFts() с синонимами
└─────────────────────────────────────────────┘
        ↓
Объединение + дедупликация + сортировка по релевантности
        ↓
Результат: List<SearchResult> (DtcResult | DiagnosisResult)
```

### 2.2 Room Entity: DtcEntity (OBD2 каталог)

```kotlin
// data/db/DtcEntity.kt
package com.example.autoelectricai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * FTS4 virtual table для полнотекстового поиска по DTC-каталогу.
 * Заполняется при первом запуске из assets/dtc_catalog.json (~5000 записей).
 */
@Entity(tableName = "dtc_catalog")
data class DtcEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "code")
    val code: String,              // "P0300", "B1234", "U0100"

    @ColumnInfo(name = "category")
    val category: String,          // "P" (Powertrain), "B" (Body), "C" (Chassis), "U" (Network)

    @ColumnInfo(name = "description_ru")
    val descriptionRu: String,     // "Пропуски воспламенения в цилиндре"

    @ColumnInfo(name = "description_en")
    val descriptionEn: String,     // "Random/Multiple Cylinder Misfire Detected"

    @ColumnInfo(name = "system")
    val system: String,            // "engine", "transmission", "abs", "airbag"

    @ColumnInfo(name = "severity")
    val severity: String,          // "critical", "warning", "info"

    @ColumnInfo(name = "common_causes")
    val commonCauses: String,      // JSON array строк: ["Свечи зажигания", "Форсунки", "Компрессия"]

    @ColumnInfo(name = "common_fixes")
    val commonFixes: String,       // JSON array строк: ["Замена свечей", "Проверка форсунок"]

    @ColumnInfo(name = "related_codes")
    val relatedCodes: String,      // "P0301,P0302,P0303" — связанные коды

    @ColumnInfo(name = "affected_brands")
    val affectedBrands: String,    // JSON: ["Toyota","BMW","VW"] или "*" (universal)

    @ColumnInfo(name = "is_generic")
    val isGeneric: Boolean = true  // true = OBD-II стандартный, false = manufacturer-specific
)
```

### 2.3 Room Entity: DtcFts (FTS4 virtual table)

```kotlin
// data/db/DtcFts.kt
package com.example.autoelectricai.data.db

import androidx.room.Fts4

/**
 * FTS4 индекс для быстрого полнотекстового поиска по DTC-каталогу.
 * Room автоматически синхронизирует данные с dtc_catalog через docid.
 */
@Fts4(contentEntity = DtcEntity::class)
@Entity(tableName = "dtc_catalog_fts")
data class DtcFts(
    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "description_ru")
    val descriptionRu: String,

    @ColumnInfo(name = "description_en")
    val descriptionEn: String,

    @ColumnInfo(name = "system")
    val system: String
)
```

### 2.4 Room Entity: DiagnosisFts (FTS4 для диагнозов)

```kotlin
// data/db/DiagnosisFts.kt
package com.example.autoelectricai.data.db

import androidx.room.Fts4

/**
 * FTS4 индекс для полнотекстового поиска по сохранённым диагнозам.
 * Автоматически синхронизируется с таблицей diagnoses.
 */
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
    val encyclopediaPlatform: String,

    @ColumnInfo(name = "encyclopediaSystem")
    val encyclopediaSystem: String,

    @ColumnInfo(name = "encyclopediaSubsystem")
    val encyclopediaSubsystem: String
)
```

### 2.5 DAO: DtcDao

```kotlin
// data/db/DtcDao.kt
package com.example.autoelectricai.data.db

import androidx.room.*

@Dao
interface DtcDao {

    /**
     * Точный поиск по коду (P0300, B1234).
     * Используется когда пользователь вводит код напрямую.
     */
    @Query("SELECT * FROM dtc_catalog WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): DtcEntity?

    /**
     * Префиксный поиск: "P03" → все коды P0300-P0399.
     */
    @Query("SELECT * FROM dtc_catalog WHERE code LIKE :prefix || '%' ORDER BY code LIMIT 20")
    suspend fun searchByPrefix(prefix: String): List<DtcEntity>

    /**
     * FTS4 полнотекстовый поиск по описанию и системе.
     * Поддерживает русский и английский текст.
     * Пример: query="мисфайер" → P0300 "Пропуски воспламенения"
     */
    @Query("""
        SELECT dtc_catalog.* FROM dtc_catalog
        JOIN dtc_catalog_fts ON dtc_catalog.rowid = dtc_catalog_fts.rowid
        WHERE dtc_catalog_fts MATCH :query
        ORDER BY 
            CASE WHEN dtc_catalog.code LIKE :exactPrefix THEN 0 ELSE 1 END,
            dtc_catalog.rowid
        LIMIT :limit
    """)
    suspend fun searchFts(query: String, exactPrefix: String = "", limit: Int = 20): List<DtcEntity>

    /**
     * Поиск по системе (engine, transmission, abs...).
     */
    @Query("SELECT * FROM dtc_catalog WHERE system = :system ORDER BY code LIMIT :limit")
    suspend fun findBySystem(system: String, limit: Int = 50): List<DtcEntity>

    /**
     * Тяжёлые коды (critical severity) — для Quick Cards.
     */
    @Query("SELECT * FROM dtc_catalog WHERE severity = 'critical' ORDER BY RANDOM() LIMIT :limit")
    suspend fun getCriticalCodes(limit: Int = 5): List<DtcEntity>

    /**
     * Случайные коды для educational content.
     */
    @Query("SELECT * FROM dtc_catalog ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomCodes(limit: Int = 10): List<DtcEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(catalog: List<DtcEntity>)

    @Query("SELECT COUNT(*) FROM dtc_catalog")
    suspend fun getCount(): Int

    @Query("DELETE FROM dtc_catalog")
    suspend fun deleteAll()
}
```

### 2.6 DAO: DiagnosisDao (расширение)

Добавить в существующий `DiagnosisDao.kt`:

```kotlin
// Добавить в DiagnosisDao:

/**
 * FTS4 поиск по диагнозам. Поддерживает русский сленг через SlangDictionary.
 * Ранжирование: successCount * 2 + likes - dislikes (community score).
 */
@Query("""
    SELECT diagnoses.* FROM diagnoses
    JOIN diagnoses_fts ON diagnoses.rowid = diagnoses_fts.rowid
    WHERE diagnoses_fts MATCH :query
    ORDER BY (successCount * 2 + likes - dislikes) DESC, createdAt DESC
    LIMIT :limit
""")
suspend fun searchFts(query: String, limit: Int = 20): List<DiagnosisEntity>

/**
 * Поиск по OBD2-кодам ошибок в сохранённых диагнозах.
 * Пример: "P0300" → все диагнозы содержащие этот код.
 */
@Query("""
    SELECT * FROM diagnoses 
    WHERE errorCodes LIKE '%' || :errorCode || '%'
    ORDER BY (successCount * 2 + likes - dislikes) DESC
    LIMIT :limit
""")
suspend fun searchByErrorCode(errorCode: String, limit: Int = 20): List<DiagnosisEntity>

/**
 * Последние N открытых/сохранённых карточек для offline-кэша.
 */
@Query("SELECT * FROM diagnoses WHERE isOfflineReady = 1 ORDER BY createdAt DESC LIMIT :limit")
suspend fun getRecentOffline(limit: Int = 50): List<DiagnosisEntity>

/**
 * Популярные диагнозы (по successCount) для Quick Cards.
 */
@Query("SELECT * FROM diagnoses WHERE successCount > 0 ORDER BY successCount DESC LIMIT :limit")
suspend fun getPopularDiagnoses(limit: Int = 10): List<DiagnosisEntity>

/**
 * Недавно добавленные (для Quick Cards «Недавно добавлено»).
 */
@Query("SELECT * FROM diagnoses ORDER BY createdAt DESC LIMIT :limit")
suspend fun getRecentDiagnoses(limit: Int = 10): List<DiagnosisEntity>
```

### 2.7 Словарь сленга (SlangDictionary)

```kotlin
// data/search/SlangDictionary.kt
package com.example.autoelectricai.data.search

/**
 * Словарь сленга автоэлектриков → нормализованные термины.
 * Используется для расширения поискового запроса перед FTS4-запросом.
 *
 * Пример: "не крутит стартер" → expanded = "no-crank starter old-crank"
 *         → FTS4 ищет по обоим вариантам.
 */
object SlangDictionary {

    data class SlangEntry(
        val slang: String,                    // "не крутит стартер"
        val normalized: String,               // "starter.no-crank"
        val aliases: List<String> = emptyList() // ["башмачит", "тяжело крутит"]
    )

    private val entries: List<SlangEntry> = listOf(
        // === Стартер ===
        SlangEntry("не крутит стартер", "starter.no-crank", listOf("стартер не работает", "не реагирует на ключ")),
        SlangEntry("щёлкает но не крутит", "starter.relay-clicks-no-crank", listOf("реле щёлкает", "клацает")),
        SlangEntry("башмачит стартер", "starter.slow-crank", listOf("тяжело крутит", "еле крутит", "вяло крутит")),
        SlangEntry("стартер крутит но не заводит", "starter.crank-no-start", listOf("крутит вхолостую")),

        // === Двигатель ===
        SlangEntry("кидает на ходу", "engine.misfire.on-load", listOf("дёргается", "подтраивает под нагрузкой")),
        SlangEntry("чихает", "engine.misfire.at-idle", listOf("чихает на холостых", "пропуски зажигания")),
        SlangEntry("троит", "engine.misfire", listOf("троение", "подтраивание")),
        SlangEntry("плавают обороты", "engine.rough-idle", listOf("плавающие обороты", "нес穩定ные обороты")),
        SlangEntry("глохнет на ходу", "engine.stall-on-move", listOf("заглох", "осёкся")),

        // === Электрика ===
        SlangEntry("пробивает массу", "ground.fault", listOf("масса", "минус", "пробой на массу")),
        SlangEntry("пропадает масса", "ground.intermittent", listOf("плавающая масса", "масса пропадает")),
        SlangEntry("утечка тока", "parasitic-drain", listOf("разряжает аккумулятор", "сажает акб", "ткинет ток")),

        // === Иммобилайзер ===
        SlangEntry("глючит иммо", "immobilizer.malfunction", listOf("иммобилайзер", "иммо блокирует", "не видит ключ")),
        SlangEntry("не видит ключ", "immobilizer.no-key-detect", listOf("антенна иммо", "не считывает ключ")),

        // === CAN-шина ===
        SlangEntry("пропадает связь с блоком", "can.bus.no-communication", listOf("нет связи с эбу", "ошибка шины", "no communication")),

        // === Индикация ===
        SlangEntry("лампа чек энджин", "indicator.check-engine-on", listOf("check engine", "check engine light", "горит чек")),
        SlangEntry("масленка горит", "indicator.oil-pressure-warning", listOf("давление масла", "масляный датчик")),
        SlangEntry("горит abs", "indicator.abs-on", listOf("значок abs", "лампа abs")),

        // === Генератор ===
        SlangEntry("воет генератор", "generator.whine", listOf("гул генератора", "шум генератора")),
        SlangEntry("нет зарядки", "generator.no-charge", listOf("генератор не даёт зарядку", "нет заряда")),

        // === Предохранители ===
        SlangEntry("перегорает предохранитель", "fuse.blown", listOf("выбивает предохранитель", "предохранитель горит")),
        SlangEntry("греется блок предохранителей", "fuse-box.overheat", listOf("греется монтажный блок"))
    )

    /**
     * Карта быстрого поиска: lowercase alias/slang → normalized.
     */
    private val lookupMap: Map<String, String> = buildMap {
        for (entry in entries) {
            put(entry.slang.lowercase(), entry.normalized)
            put(entry.normalized.lowercase(), entry.normalized)
            for (alias in entry.aliases) {
                put(alias.lowercase(), entry.normalized)
            }
        }
    }

    /**
     * Нормализует запрос пользователя.
     * 1. Ищет точное совпадение в словаре
     * 2. Ищет частичные совпадения (contains)
     * 3. Возвращает оригинальный запрос + найденные нормализованные термины
     *
     * Пример: "стартер не крутит на холодную"
     *   → matches: ["starter.no-crank"]
     *   → expanded query: "стартер не крутит на холодную OR starter.no-crank"
     */
    fun normalize(query: String): SearchExpansion {
        val lower = query.lowercase().trim()
        val matchedNormalized = mutableSetOf<String>()

        // Точное совпадение
        lookupMap[lower]?.let { matchedNormalized.add(it) }

        // Частичные совпадения (слово за словом)
        val words = lower.split(Regex("\\s+"))
        for (word in words) {
            lookupMap[word]?.let { matchedNormalized.add(it) }
        }

        // Проверка contains для длинных запросов
        for ((slang, normalized) in lookupMap) {
            if (slang.length > 3 && lower.contains(slang)) {
                matchedNormalized.add(normalized)
            }
        }

        return SearchExpansion(
            original = query,
            normalized = matchedNormalized.toList(),
            expandedQuery = buildExpandedQuery(query, matchedNormalized.toList())
        )
    }

    private fun buildExpandedQuery(original: String, normalized: List<String>): String {
        if (normalized.isEmpty()) return original
        // FTS4 OR-синтаксис: "query OR normalized1 OR normalized2"
        val parts = mutableListOf(original)
        parts.addAll(normalized)
        return parts.joinToString(" OR ")
    }

    data class SearchExpansion(
        val original: String,
        val normalized: List<String>,
        val expandedQuery: String
    )
}
```

### 2.8 SearchRepository (единая точка входа)

```kotlin
// data/search/SearchRepository.kt
package com.example.autoelectricai.data.search

import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.db.DtcDao
import com.example.autoelectricai.data.db.DtcEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единая точка входа для глобального поиска.
 * Координирует DTC-каталог, Diagnosis FTS4, и сленг-словарь.
 *
 * Алгоритм:
 * 1. Определяем тип запроса (DTC-код vs текст)
 * 2. Нормализуем через SlangDictionary
 * 3. Выполняем параллельные запросы к DTC и Diagnosis FTS4
 * 4. Объединяем, дедуплицируем, сортируем
 */
@Singleton
class SearchRepository @Inject constructor(
    private val dtcDao: DtcDao,
    private val diagnosisDao: DiagnosisDao
) {
    companion object {
        // Regex для определения OBD2-кода: P/B/C/U + 4 цифры
        private val DTC_PATTERN = Regex("^[PpBbCcUu]\\d{4}$")
        private val DTC_PREFIX_PATTERN = Regex("^[PpBbCcUu]\\d{1,3}$")
    }

    /**
     * Основной метод поиска. Возвращает объединённый результат.
     */
    suspend fun search(query: String, brandFilter: String = ""): SearchResult {
        val trimmed = query.trim()
        if (trimmed.length < 2) return SearchResult(emptyList(), emptyList())

        // Определяем тип запроса
        val isFullDtc = DTC_PATTERN.matches(trimmed)
        val isDtcPrefix = DTC_PREFIX_PATTERN.matches(trimmed)

        val dtcResults = mutableListOf<DtcEntity>()
        val diagnosisResults = mutableListOf<DiagnosisEntity>()

        if (isFullDtc) {
            // Точный DTC-код: ищем в каталоге и в диагнозах
            dtcDao.findByCode(trimmed.uppercase())?.let { dtcResults.add(it) }
            diagnosisResults.addAll(diagnosisDao.searchByErrorCode(trimmed.uppercase()))
        } else if (isDtcPrefix) {
            // Префикс: P03 → все P03xx
            dtcResults.addAll(dtcDao.searchByPrefix(trimmed.uppercase()))
        }

        // Текстовый поиск (всегда, даже если нашли DTC)
        val expansion = SlangDictionary.normalize(trimmed)

        // FTS4 запрос: используем расширенный запрос
        val ftsQuery = expansion.expandedQuery
        try {
            diagnosisResults.addAll(diagnosisDao.searchFts(ftsQuery, limit = 20))
        } catch (e: Exception) {
            // FTS4 может упасть на невалидном синтаксисе — fallback на LIKE
            diagnosisResults.addAll(diagnosisDao.searchDiagnoses(trimmed, brandFilter))
        }

        // DTC FTS4 (по описанию)
        if (!isFullDtc && !isDtcPrefix) {
            try {
                dtcResults.addAll(dtcDao.searchFts(ftsQuery, limit = 10))
            } catch (e: Exception) {
                // Fallback: LIKE поиск
            }
        }

        // Дедупликация Diagnosis
        val uniqueDiagnoses = diagnosisResults.distinctBy { it.cloudId ?: it.id }
            .sortedByDescending { it.successCount * 2 + it.likes - it.dislikes }
            .take(20)

        return SearchResult(
            dtcResults = dtcResults.distinctBy { it.code }.take(10),
            diagnosisResults = uniqueDiagnoses
        )
    }

    /**
     * Быстрый поиск для автодополнения (debounced, 300ms).
     * Возвращает топ-5 для каждого типа.
     */
    suspend fun quickSearch(query: String): SearchResult {
        val trimmed = query.trim()
        if (trimmed.length < 2) return SearchResult(emptyList(), emptyList())

        val dtcResults = mutableListOf<DtcEntity>()
        val diagnosisResults = mutableListOf<DiagnosisEntity>()

        // DTC: точный + префикс
        if (DTC_PATTERN.matches(trimmed)) {
            dtcDao.findByCode(trimmed.uppercase())?.let { dtcResults.add(it) }
        } else if (DTC_PREFIX_PATTERN.matches(trimmed)) {
            dtcResults.addAll(dtcDao.searchByPrefix(trimmed.uppercase()))
        }

        // Diagnosis: FTS4
        try {
            val expansion = SlangDictionary.normalize(trimmed)
            diagnosisResults.addAll(diagnosisDao.searchFts(expansion.expandedQuery, limit = 5))
        } catch (e: Exception) {
            diagnosisResults.addAll(diagnosisDao.searchDiagnoses(trimmed))
        }

        return SearchResult(
            dtcResults = dtcResults.take(5),
            diagnosisResults = diagnosisResults.take(5)
        )
    }

    data class SearchResult(
        val dtcResults: List<DtcEntity>,
        val diagnosisResults: List<DiagnosisEntity>
    )
}
```

### 2.9 UI: SearchScreen

```kotlin
// ui/search/SearchScreen.kt
package com.example.autoelectricai.ui.search

/**
 * Экран глобального поиска.
 *
 * Структура:
 * ┌──────────────────────────────────────┐
 * │ 🔍 [    Поиск по кодам, симптомам... ]│  ← поле поиска 56dp
 * ├──────────────────────────────────────┤
 * │ Быстрый доступ:                      │
 * │ [P0300] [P0171] [P0174] [U0100]     │  ← chips с частыми кодами
 * ├──────────────────────────────────────┤
 * │ Недавние запросы:                    │
 * │ 🕐 "не крутит стартер"              │  ← история (до 10)
 * │ 🕐 "P0300"                          │
 * ├──────────────────────────────────────┤
 * │ ── Найдено в DTC-каталоге ──         │
 * │ ┌──────────────────────────────┐    │
 * │ │ P0300  Пропуски воспламенения │    │  ← DTC карточка
 * │ │ 🔴 Критично │ Двигатель       │    │
 * │ └──────────────────────────────┘    │
 * ├──────────────────────────────────────┤
 * │ ── Найдено в базе решений ──        │
 * │ ┌──────────────────────────────┐    │
 * │ │ BMW E90 • Стартер            │    │  ← Diagnosis карточка
 * │ │ "Не крутит стартер, щёлкает  │    │
 * │ │  реле..."                     │    │
 * │ │ ✓ 12 решений                 │    │
 * │ └──────────────────────────────┘    │
 * └──────────────────────────────────────┘
 */

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoelectricai.theme.*

// Компонент SearchResultCard — единая карточка для DTC и Diagnosis
@Composable
fun SearchResultCard(
    // DTC данные (nullable)
    dtcCode: String? = null,
    dtcDescription: String? = null,
    dtcSeverity: String? = null,
    dtcSystem: String? = null,
    // Diagnosis данные (nullable)
    diagnosisBrand: String? = null,
    diagnosisModel: String? = null,
    diagnosisSystem: String? = null,
    diagnosisSymptoms: String? = null,
    diagnosisSuccessCount: Int = 0,
    // Callbacks
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (dtcCode != null) {
                // DTC режим
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (dtcSeverity) {
                                    "critical" -> ErrorRed.copy(alpha = 0.2f)
                                    "warning" -> WarningOrange.copy(alpha = 0.2f)
                                    else -> LocalHitBlue.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            dtcCode,
                            color = when (dtcSeverity) {
                                "critical" -> ErrorRed
                                "warning" -> WarningOrange
                                else -> LocalHitBlue
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            dtcDescription ?: "",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                        if (dtcSystem != null) {
                            Text(
                                dtcSystem.replaceFirstChar { it.uppercase() },
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else if (diagnosisBrand != null) {
                // Diagnosis режим
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Build,
                            null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row {
                            Text(
                                "$diagnosisBrand $diagnosisModel",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (diagnosisSystem != null) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "• $diagnosisSystem",
                                    color = AmberPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (diagnosisSymptoms != null) {
                            Text(
                                diagnosisSymptoms.take(80) + if (diagnosisSymptoms.length > 80) "..." else "",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }
                    }
                    if (diagnosisSuccessCount > 0) {
                        Text(
                            "✓ $diagnosisSuccessCount",
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
```

### 2.10 ViewModel: SearchViewModel

```kotlin
// ui/search/SearchViewModel.kt
package com.example.autoelectricai.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.search.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<SearchRepository.SearchResult>(
        SearchRepository.SearchResult(emptyList(), emptyList())
    )
    val results: StateFlow<SearchRepository.SearchResult> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _recentQueries = MutableStateFlow<List<String>>(emptyList())
    val recentQueries: StateFlow<List<String>> = _recentQueries.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.length < 2) {
            _results.value = SearchRepository.SearchResult(emptyList(), emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // debounce 300ms
            _isSearching.value = true
            try {
                _results.value = searchRepository.search(newQuery)
                // Сохраняем в историю
                addRecentQuery(newQuery)
            } catch (e: Exception) {
                _results.value = SearchRepository.SearchResult(emptyList(), emptyList())
            } finally {
                _isSearching.value = false
            }
        }
    }

    private fun addRecentQuery(query: String) {
        val current = _recentQueries.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > 10) current.removeLast()
        _recentQueries.value = current
    }

    fun clearRecentQueries() {
        _recentQueries.value = emptyList()
    }
}
```

---

## 3. Quick Cards

### 3.1 Назначение и расположение

Quick Cards отображаются **на экране Diagnosis** (MainScreen → Diagnosis tab), **над 3-шаговым wizard**, когда пользователь ещё не начал ввод данных.

**Цель:** дать пользователю мгновенный доступ к популярному контенту, не начиная diagnostic flow.

### 3.2 Архитектура данных

Quick Cards загружаются из Room:
- **«Популярное сегодня»** — топ-5 диагнозов по `successCount` (community-верифицированные)
- **«Недавно добавлено»** — топ-5 последних по `createdAt`

Данные кэшируются в ViewModel при первом открытии экрана.

### 3.3 UI-компонент: QuickCards

```kotlin
// ui/main/QuickCards.kt
package com.example.autoelectricai.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.theme.*

/**
 * Секция Quick Cards на главном экране.
 *
 * Структура:
 * ┌──────────────────────────────────────────────────┐
 * │ 🔥 Популярное сегодня                           │
 * │ ┌────────────┐ ┌────────────┐ ┌────────────┐   │
 * │ │ BMW E90    │ │ Toyota     │ │ VW Golf    │   │
 * │ │ Стартер    │ │ Check Eng  │ │ Иммо       │   │
 * │ │ ✓ 24       │ │ ✓ 18       │ │ ✓ 12       │   │
 * │ └────────────┘ └────────────┘ └────────────┘   │
 * ├──────────────────────────────────────────────────┤
 * │ 🆕 Недавно добавлено                            │
 * │ ┌────────────┐ ┌────────────┐ ┌────────────┐   │
 * │ │ ...        │ │ ...        │ │ ...        │   │
 * │ └────────────┘ └────────────┘ └────────────┘   │
 * └──────────────────────────────────────────────────┘
 */
@Composable
fun QuickCards(
    popularDiagnoses: List<DiagnosisEntity>,
    recentDiagnoses: List<DiagnosisEntity>,
    onDiagnosisClick: (DiagnosisEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Популярное сегодня
        if (popularDiagnoses.isNotEmpty()) {
            QuickCardSection(
                title = "Популярное сегодня",
                icon = Icons.Default.TrendingUp,
                iconColor = AmberPrimary,
                items = popularDiagnoses,
                onDiagnosisClick = onDiagnosisClick
            )
            Spacer(Modifier.height(16.dp))
        }

        // Недавно добавлено
        if (recentDiagnoses.isNotEmpty()) {
            QuickCardSection(
                title = "Недавно добавлено",
                icon = Icons.Default.NewReleases,
                iconColor = LocalHitBlue,
                items = recentDiagnoses,
                onDiagnosisClick = onDiagnosisClick
            )
        }
    }
}

@Composable
private fun QuickCardSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    items: List<DiagnosisEntity>,
    onDiagnosisClick: (DiagnosisEntity) -> Unit
) {
    Column {
        // Заголовок секции
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))

        // Горизонтальный список карточек
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items.take(5), key = { it.cloudId ?: it.id }) { entity ->
                QuickCardItem(entity = entity, onClick = { onDiagnosisClick(entity) })
            }
        }
    }
}

@Composable
private fun QuickCardItem(
    entity: DiagnosisEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Brand/Model
            Text(
                "${entity.carBrand} ${entity.carModel}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))

            // System badge
            if (entity.system.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmberPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        entity.system.take(15),
                        color = AmberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            // Symptoms preview
            Text(
                entity.symptoms.take(40) + if (entity.symptoms.length > 40) "..." else "",
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            // Success count
            if (entity.successCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${entity.successCount}× помогло",
                        color = SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
```

### 3.4 Интеграция в DiagnosisScreen

Добавить в `DiagnosisScreen.kt` **над StepCar** (шаг 1 wizard):

```kotlin
// В DiagnosisScreen, перед AnimatedContent:
// Quick Cards отображаются ТОЛЬКО на шаге CAR и ТОЛЬКО когда форма пуста
if (currentStep == DiagnosisStep.CAR && carBrand.isBlank()) {
    QuickCards(
        popularDiagnoses = popularDiagnoses,
        recentDiagnoses = recentDiagnoses,
        onDiagnosisClick = { entity ->
            // Заполняем форму данными из карточки и переходим к результату
            viewModel.carBrand.value = entity.carBrand
            viewModel.carModel.value = entity.carModel
            viewModel.carYear.value = entity.carYear
            viewModel.selectedSystem.value = entity.system
            viewModel.symptoms.value = entity.symptoms
            viewModel.errorCodes.value = entity.errorCodes
            viewModel.selectSuggestion(entity)
        },
        modifier = Modifier.padding(top = 8.dp)
    )
    Spacer(Modifier.height(16.dp))
}
```

---

## 4. Typography & Styling

### 4.1 Текущее состояние (`Type.kt`)

Сейчас определён только `bodyLarge` (16sp). Остальные стили берутся из дефолтов Material3, что даёт:
- `titleLarge`: 22sp (нормально)
- `titleMedium`: 16sp (слишком мелко для заголовков)
- `bodyMedium`: 14sp (слишком мелко для body text в гараже)
- `labelSmall`: 11sp (ок для меток)

### 4.2 Рекомендуемые значения

```kotlin
// theme/Type.kt
package com.example.autoelectricai.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Кастомная типографика для среды использования:
 * - Тёмный гараж
 * - Грязные руки (нужен крупный текст)
 * - Плохое освещение
 *
 * Все значения увеличены относительно Material3 defaults.
 * Минимальный body text: 16sp (vs 14sp default).
 * Заголовки: ≥20sp (vs 16sp default для titleMedium).
 */
val Typography = Typography(
    // === Display (экраны-заставки, splash) ===
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // === Headlines (заголовки секций) ===
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // === Titles (заголовки экранов и карточек) ===
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,      // ↑ от 16sp default
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,      // ↑ от 14sp default
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),

    // === Body (основной текст) ===
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,      // Уже 16sp — оставляем
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,      // Стандарт, ок
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // === Labels (кнопки, метки, badges) ===
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,      // ↑ для кнопок (крупные tap targets)
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,      // ↑ от 12sp default
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,      // ↑ от 11sp default
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### 4.3 Изменения в компонентах

В следующих файлах заменить hardcoded `fontSize` на `MaterialTheme.typography.*`:

| Файл | Текущее | Замена на |
|---|---|---|
| `DiagnosisScreen.kt` | `fontSize = 14.sp` (body) | `MaterialTheme.typography.bodyLarge` |
| `DiagnosisScreen.kt` | `fontSize = 18.sp` (title) | `MaterialTheme.typography.titleMedium` |
| `DiagnosisScreen.kt` | `fontSize = 16.sp` (button) | `MaterialTheme.typography.labelLarge` |
| `DiagnosisScreen.kt` | `fontSize = 12.sp` (hint) | `MaterialTheme.typography.labelSmall` |
| `KnowledgeBaseScreen.kt` | `fontSize = 14.sp` | `MaterialTheme.typography.bodyLarge` |
| `KnowledgeBaseScreen.kt` | `fontSize = 11.sp` | `MaterialTheme.typography.labelSmall` |

**Важно:** в первое время допустимо оставить hardcoded значения в компонентах, где размер критичен (кнопки 56dp, теги). Постепенная миграция на `MaterialTheme.typography` — отдельная задача.

---

## 5. Offline-кэш

### 5.1 Архитектура

```
Открытие карточки DiagnosisScreen
        ↓
OfflineCacheManager.trackAccess(entity.id)  ← обновляет lastAccessedAt
        ↓
WorkManager (periodic, 6ч) или OneTimeWorkRequest при установке сети
        ↓
OfflineCacheWorker.doWork():
    1. SELECT * FROM diagnoses WHERE isOfflineReady = 1 
       ORDER BY lastAccessedAt DESC LIMIT 50
    2. Для каждой записи: solution JSON уже в Room → просто помечаем cached=1
    3. Остальные: удаляем из кэша (LRU eviction)
        ↓
OfflineCacheDao:
    - markCached(id)
    - evictOldest(excludeIds)
    - getCachedDiagnoses()
    - getCachedCount()
```

### 5.2 Room Entity: OfflineCacheEntity

```kotlin
// data/db/OfflineCacheEntity.kt
package com.example.autoelectricai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Таблица для управления offline-кэшем.
 * Связана с diagnoses через diagnosisId.
 * Отслеживает порядок доступа для LRU-эвикции.
 */
@Entity(tableName = "offline_cache")
data class OfflineCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "diagnosis_id")
    val diagnosisId: Long,          // FK → diagnoses.id

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long,             // timestamp когда закэшировали

    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: Long,       // timestamp последнего открытия

    @ColumnInfo(name = "access_count")
    val accessCount: Int = 1,       // сколько раз открывали

    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long = 0         // размер JSON в байтах (для контроля лимита)
)
```

### 5.3 DAO: OfflineCacheDao

```kotlin
// data/db/OfflineCacheDao.kt
package com.example.autoelectricai.data.db

import androidx.room.*

@Dao
interface OfflineCacheDao {

    /**
     * Отметить карточку как закэшированную (или обновить lastAccessedAt).
     */
    @Query("""
        INSERT OR REPLACE INTO offline_cache 
        (diagnosis_id, cached_at, last_accessed_at, access_count, size_bytes)
        VALUES (
            :diagnosisId,
            COALESCE((SELECT cached_at FROM offline_cache WHERE diagnosis_id = :diagnosisId), :now),
            :now,
            COALESCE((SELECT access_count FROM offline_cache WHERE diagnosis_id = :diagnosisId), 0) + 1,
            :sizeBytes
        )
    """)
    suspend fun upsert(diagnosisId: Long, now: Long = System.currentTimeMillis(), sizeBytes: Long = 0)

    /**
     * Получить все закэшированные ID, отсортированные по LRU (самые старые первые).
     */
    @Query("SELECT diagnosis_id FROM offline_cache ORDER BY last_accessed_at ASC")
    suspend fun getCachedIds(): List<Long>

    /**
     * Получить количество закэшированных элементов.
     */
    @Query("SELECT COUNT(*) FROM offline_cache")
    suspend fun getCachedCount(): Int

    /**
     * Удалить из кэша конкретную запись.
     */
    @Query("DELETE FROM offline_cache WHERE diagnosis_id = :diagnosisId")
    suspend fun evict(diagnosisId: Long)

    /**
     * Эвикция: удалить N самых старых записей (LRU).
     * Возвращает IDs удалённых записей.
     */
    @Query("""
        DELETE FROM offline_cache 
        WHERE id IN (
            SELECT id FROM offline_cache 
            ORDER BY last_accessed_at ASC 
            LIMIT :count
        )
    """)
    suspend fun evictOldest(count: Int)

    /**
     * Очистить весь кэш.
     */
    @Query("DELETE FROM offline_cache")
    suspend fun clearAll()

    /**
     * Получить список diagnosis_id, которые нужно выгрузить из кэша
     * (те, что превышают лимит 50).
     */
    @Query("""
        SELECT diagnosis_id FROM offline_cache 
        ORDER BY last_accessed_at ASC 
        LIMIT -1 OFFSET :keepCount
    """)
    suspend fun getExcessIds(keepCount: Int = 50): List<Long>
}
```

### 5.4 OfflineCacheManager

```kotlin
// data/offline/OfflineCacheManager.kt
package com.example.autoelectricai.data.offline

import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.OfflineCacheDao
import com.example.autoelectricai.utils.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер offline-кэша.
 * Управляет LRU-эвикцией и отслеживанием доступа.
 *
 * Лимит: 50 карточек. При превышении — удаляем самые старые.
 * При открытии карточки — обновляем lastAccessedAt.
 */
@Singleton
class OfflineCacheManager @Inject constructor(
    private val cacheDao: OfflineCacheDao,
    private val diagnosisDao: DiagnosisDao
) {
    companion object {
        private const val TAG = "OfflineCacheManager"
        private const val MAX_CACHE_SIZE = 50
    }

    /**
     * Вызывается при открытии карточки DiagnosisScreen.
     * Помечает запись как «недавно использованную».
     */
    suspend fun trackAccess(diagnosisId: Long) {
        val entity = diagnosisDao.getById(diagnosisId) ?: return
        val sizeBytes = entity.solution.toByteArray().size.toLong()
        cacheDao.upsert(diagnosisId, sizeBytes = sizeBytes)
        enforceMaxSize()
    }

    /**
     * Принудительно пометить как offline-ready (после сохранения в закладки).
     */
    suspend fun markOfflineReady(diagnosisId: Long) {
        val entity = diagnosisDao.getById(diagnosisId) ?: return
        diagnosisDao.markAsSuccessful(diagnosisId) // isOfflineReady = 1
        val sizeBytes = entity.solution.toByteArray().size.toLong()
        cacheDao.upsert(diagnosisId, sizeBytes = sizeBytes)
        enforceMaxSize()
    }

    /**
     * Получить все offline-ready диагнозы (для отображения без сети).
     */
    suspend fun getCachedDiagnoses() = diagnosisDao.getRecentOffline(MAX_CACHE_SIZE)

    /**
     * Получить количество закэшированных элементов.
     */
    suspend fun getCachedCount() = cacheDao.getCachedCount()

    /**
     * Очистить весь кэш.
     */
    suspend fun clearCache() {
        cacheDao.clearAll()
        AppLogger.i(TAG, "Offline cache cleared")
    }

    /**
     * LRU-эвикция: если больше MAX_CACHE_SIZE — удаляем самые старые.
     */
    private suspend fun enforceMaxSize() {
        val count = cacheDao.getCachedCount()
        if (count > MAX_CACHE_SIZE) {
            val excess = count - MAX_CACHE_SIZE
            cacheDao.evictOldest(excess)
            AppLogger.i(TAG, "Evicted $excess old cache entries (limit: $MAX_CACHE_SIZE)")
        }
    }
}
```

### 5.5 DAO extension: getById

Добавить в `DiagnosisDao.kt`:

```kotlin
@Query("SELECT * FROM diagnoses WHERE id = :id LIMIT 1")
suspend fun getById(id: Long): DiagnosisEntity?
```

### 5.6 WorkManager: OfflineCacheWorker

```kotlin
// data/offline/OfflineCacheWorker.kt
package com.example.autoelectricai.data.offline

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.OfflineCacheDao
import com.example.autoelectricai.utils.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Фоновый worker для поддержания offline-кэша.
 *
 * Задачи:
 * 1. Пометить недавние diagnosis как isOfflineReady (если ещё не помечены)
 * 2. Выполнить LRU-эвикцию (удалить записи за пределами лимита 50)
 * 3. Логировать статистику кэша
 *
 * Запуск: периодический (6ч) через WorkManager + OneTimeWorkRequest при первом запуске.
 */
@HiltWorker
class OfflineCacheWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val diagnosisDao: DiagnosisDao,
    private val cacheDao: OfflineCacheDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "OfflineCacheWorker"
        private const val MAX_CACHE = 50
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            AppLogger.i(TAG, "Starting offline cache maintenance")

            // 1. Помечаем последние 50 диагнозов как offline-ready
            val recentDiagnoses = diagnosisDao.getRecentDiagnoses(MAX_CACHE)
            for (entity in recentDiagnoses) {
                if (!entity.isOfflineReady) {
                    diagnosisDao.markAsSuccessful(entity.id)
                }
                // Убеждаемся что в кэше есть запись
                cacheDao.upsert(
                    diagnosisId = entity.id,
                    sizeBytes = entity.solution.toByteArray().size.toLong()
                )
            }

            // 2. LRU-эвикция
            val count = cacheDao.getCachedCount()
            if (count > MAX_CACHE) {
                val excess = count - MAX_CACHE
                cacheDao.evictOldest(excess)
                AppLogger.i(TAG, "Evicted $excess excess cache entries")
            }

            // 3. Статистика
            val offlineCount = diagnosisDao.getOfflineCount()
            val cachedCount = cacheDao.getCachedCount()
            AppLogger.i(TAG, "Cache maintenance done. Offline-ready: $offlineCount, Cached: $cachedCount")

            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Cache maintenance failed", e)
            Result.retry()
        }
    }
}
```

### 5.7 Регистрация Worker в MainActivity

Добавить в `MainActivity.kt`:

```kotlin
// В scheduleCloudSync(), добавить после CloudSyncWork:
private fun scheduleOfflineCacheSync() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Работает оффлайн!
        .build()

    val cacheRequest = PeriodicWorkRequestBuilder<OfflineCacheWorker>(6, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "OfflineCacheWork",
        ExistingPeriodicWorkPolicy.KEEP,
        cacheRequest
    )
}
```

---

## 6. Миграция БД (v6 → v7)

```kotlin
// В AppDatabase.kt добавить:

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. DTC каталог
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS dtc_catalog (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                code TEXT NOT NULL,
                category TEXT NOT NULL,
                description_ru TEXT NOT NULL,
                description_en TEXT NOT NULL,
                system TEXT NOT NULL,
                severity TEXT NOT NULL,
                common_causes TEXT NOT NULL DEFAULT '[]',
                common_fixes TEXT NOT NULL DEFAULT '[]',
                related_codes TEXT NOT NULL DEFAULT '',
                affected_brands TEXT NOT NULL DEFAULT '*',
                is_generic INTEGER NOT NULL DEFAULT 1
            )
        """)

        // 2. DTC FTS4 virtual table
        database.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS dtc_catalog_fts 
            USING fts4(code, description_ru, description_en, system, 
                       content='dtc_catalog', content_rowid='id')
        """)

        // 3. Diagnosis FTS4 virtual table
        database.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS diagnoses_fts 
            USING fts4(carBrand, carModel, system, symptoms, errorCodes, solution,
                       encyclopediaPlatform, encyclopediaSystem, encyclopediaSubsystem,
                       content='diagnoses', content_rowid='id')
        """)

        // 4. Offline cache table
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

        // 5. Триггеры синхронизации FTS4
        // При вставке в diagnoses → обновляем diagnoses_fts
        database.execSQL("""
            CREATE TRIGGER IF NOT EXISTS diagnoses_ai AFTER INSERT ON diagnoses BEGIN
                INSERT INTO diagnoses_fts(rowid, carBrand, carModel, system, symptoms, errorCodes, solution,
                    encyclopediaPlatform, encyclopediaSystem, encyclopediaSubsystem)
                VALUES (new.id, new.carBrand, new.carModel, new.system, new.symptoms, new.errorCodes, new.solution,
                    new.encyclopediaPlatform, new.encyclopediaSystem, new.encyclopediaSubsystem);
            END
        """)

        // При обновлении diagnoses → обновляем diagnoses_fts
        database.execSQL("""
            CREATE TRIGGER IF NOT EXISTS diagnoses_au AFTER UPDATE ON diagnoses BEGIN
                DELETE FROM diagnoses_fts WHERE rowid = old.id;
                INSERT INTO diagnoses_fts(rowid, carBrand, carModel, system, symptoms, errorCodes, solution,
                    encyclopediaPlatform, encyclopediaSystem, encyclopediaSubsystem)
                VALUES (new.id, new.carBrand, new.carModel, new.system, new.symptoms, new.errorCodes, new.solution,
                    new.encyclopediaPlatform, new.encyclopediaSystem, new.encyclopediaSubsystem);
            END
        """)

        // При удалении diagnoses → удаляем из diagnoses_fts
        database.execSQL("""
            CREATE TRIGGER IF NOT EXISTS diagnoses_ad AFTER DELETE ON diagnoses BEGIN
                DELETE FROM diagnoses_fts WHERE rowid = old.id;
            END
        """)

        // 6. Индексы для производительности
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_dtc_code ON dtc_catalog(code)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_dtc_system ON dtc_catalog(system)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_dtc_severity ON dtc_catalog(severity)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_offline_cache_diagnosis ON offline_cache(diagnosis_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_offline_cache_accessed ON offline_cache(last_accessed_at)")
    }
}
```

**Обновить AppModule.kt:**

```kotlin
// В provideDatabase():
fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, "autoelectric.db")
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7  // ← добавить
        )
        .build()

// Добавить Provides:
@Provides
fun provideDtcDao(db: AppDatabase): DtcDao = db.dtcDao()

@Provides
fun provideOfflineCacheDao(db: AppDatabase): OfflineCacheDao = db.offlineCacheDao()
```

**Обновить AppDatabase.kt:**

```kotlin
@Database(
    entities = [
        DiagnosisEntity::class,
        DtcEntity::class,
        OfflineCacheEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diagnosisDao(): DiagnosisDao
    abstract fun dtcDao(): DtcDao
    abstract fun offlineCacheDao(): OfflineCacheDao
    // ...
}
```

---

## 7. Список файлов

### Новые файлы (создать):

| # | Путь | Описание |
|---|---|---|
| 1 | `data/db/DtcEntity.kt` | Room Entity для DTC-каталога |
| 2 | `data/db/DtcFts.kt` | FTS4 virtual table для DTC |
| 3 | `data/db/DiagnosisFts.kt` | FTS4 virtual table для Diagnosis |
| 4 | `data/db/DtcDao.kt` | DAO для DTC-каталога |
| 5 | `data/db/OfflineCacheEntity.kt` | Room Entity для offline-кэша |
| 6 | `data/db/OfflineCacheDao.kt` | DAO для offline-кэша |
| 7 | `data/search/SearchRepository.kt` | Единая точка входа для поиска |
| 8 | `data/search/SlangDictionary.kt` | Словарь сленга |
| 9 | `data/offline/OfflineCacheManager.kt` | Менеджер offline-кэша |
| 10 | `data/offline/OfflineCacheWorker.kt` | WorkManager worker |
| 11 | `ui/search/SearchScreen.kt` | Экран глобального поиска |
| 12 | `ui/search/SearchViewModel.kt` | ViewModel для поиска |
| 13 | `ui/search/DtcDetailScreen.kt` | Экран DTC-карточки |
| 14 | `ui/search/SearchComponents.kt` | Переиспользуемые компоненты |
| 15 | `ui/main/QuickCards.kt` | Компонент Quick Cards |
| 16 | `assets/dtc_catalog.json` | Начальный набор DTC-кодов (5000+) |

### Изменяемые файлы:

| # | Путь | Изменение |
|---|---|---|
| 1 | `data/db/AppDatabase.kt` | Версия 7, новые entities, миграция |
| 2 | `data/db/DiagnosisDao.kt` | +6 новых DAO-методов |
| 3 | `di/AppModule.kt` | +2 Provides (DtcDao, OfflineCacheDao) |
| 4 | `ui/main/MainScreen.kt` | +1 таб «Поиск», route |
| 5 | `theme/Type.kt` | Полная переработка typography |
| 6 | `ui/diagnosis/DiagnosisScreen.kt` | +Quick Cards above wizard |
| 7 | `ui/diagnosis/DiagnosisViewModel.kt` | +quickDiagnoses, recentDiagnoses |
| 8 | `data/DiagnosisRepository.kt` | +getPopularDiagnoses, getRecentDiagnoses |
| 9 | `MainActivity.kt` | +scheduleOfflineCacheSync() |

---

*Документ подготовлен для реализации Фазы 1 (Quick Wins). Все примеры кода совместимы с текущей архитектурой проекта (Hilt, Room, Compose).*
