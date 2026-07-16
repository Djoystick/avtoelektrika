package com.example.autoelectricai.data.search

object SlangDictionary {

    data class SlangEntry(
        val slang: String,
        val normalized: String,
        val aliases: List<String> = emptyList()
    )

    private val slangEntries: List<SlangEntry> = listOf(
        SlangEntry("не крутит стартер", "starter.no-crank", listOf("стартер не работает", "не реагирует на ключ")),
        SlangEntry("щёлкает но не крутит", "starter.relay-clicks-no-crank", listOf("реле щёлкает", "клацает")),
        SlangEntry("башмачит стартер", "starter.slow-crank", listOf("тяжело крутит", "еле крутит", "вяло крутит")),
        SlangEntry("стартер крутит но не заводит", "starter.crank-no-start", listOf("крутит вхолостую")),
        SlangEntry("кидает на ходу", "engine.misfire.on-load", listOf("дёргается", "подтраивает под нагрузкой")),
        SlangEntry("чихает", "engine.misfire.at-idle", listOf("чихает на холостых", "пропуски зажигания")),
        SlangEntry("троит", "engine.misfire", listOf("троение", "подтраивание")),
        SlangEntry("плавают обороты", "engine.rough-idle", listOf("плавающие обороты", "нестабильные обороты")),
        SlangEntry("глохнет на ходу", "engine.stall-on-move", listOf("заглох", "осёкся")),
        SlangEntry("пробивает массу", "ground.fault", listOf("масса", "минус", "пробой на массу")),
        SlangEntry("пропадает масса", "ground.intermittent", listOf("плавающая масса", "масса пропадает")),
        SlangEntry("утечка тока", "parasitic-drain", listOf("разряжает аккумулятор", "сажает акб", "ткинет ток")),
        SlangEntry("глючит иммо", "immobilizer.malfunction", listOf("иммобилайзер", "иммо блокирует", "не видит ключ")),
        SlangEntry("не видит ключ", "immobilizer.no-key-detect", listOf("антенна иммо", "не считывает ключ")),
        SlangEntry("пропадает связь с блоком", "can.bus.no-communication", listOf("нет связи с эбу", "ошибка шины", "no communication")),
        SlangEntry("лампа чек энджин", "indicator.check-engine-on", listOf("check engine", "check engine light", "горит чек")),
        SlangEntry("масленка горит", "indicator.oil-pressure-warning", listOf("давление масла", "масляный датчик")),
        SlangEntry("горит abs", "indicator.abs-on", listOf("значок abs", "лампа abs")),
        SlangEntry("воет генератор", "generator.whine", listOf("гул генератора", "шум генератора")),
        SlangEntry("нет зарядки", "generator.no-charge", listOf("генератор не даёт зарядку", "нет заряда")),
        SlangEntry("перегорает предохранитель", "fuse.blown", listOf("выбивает предохранитель", "предохранитель горит")),
        SlangEntry("греется блок предохранителей", "fuse-box.overheat", listOf("греется монтажный блок"))
    )

    private val lookupMap: Map<String, String> = buildMap {
        for (entry in slangEntries) {
            put(entry.slang.lowercase(), entry.normalized)
            put(entry.normalized.lowercase(), entry.normalized)
            for (alias in entry.aliases) {
                put(alias.lowercase(), entry.normalized)
            }
        }
    }

    fun normalize(query: String): SearchExpansion {
        val lower = query.lowercase().trim()
        val matchedNormalized = mutableSetOf<String>()

        lookupMap[lower]?.let { matchedNormalized.add(it) }

        val words = lower.split(Regex("\\s+"))
        for (word in words) {
            lookupMap[word]?.let { matchedNormalized.add(it) }
        }

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
