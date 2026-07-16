package com.example.autoelectricai.data.encyclopedia

import androidx.compose.ui.graphics.Color
import com.example.autoelectricai.R

data class EncCountry(
    val id: String,
    val displayName: String,
    val flagEmoji: String,
    val brands: List<EncBrand>
)

data class EncBrand(
    val id: String,
    val displayName: String,
    val shortName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val logoResId: Int?,
    val platforms: List<EncPlatform>
)

data class EncPlatform(
    val id: String,
    val displayName: String,
    val icon: String,
    val startYear: Int,
    val endYear: Int?,
    val systems: List<EncSystem>
)

data class EncSystem(
    val id: String,
    val displayName: String,
    val icon: String,
    val subsystems: List<String>
)

object EncyclopediaCatalog {

    private val brandsFlat: List<EncBrand> = listOf(

        // ─── РОССИЯ ───────────────────────────────────────────────────────────

        EncBrand(
            id = "vaz", displayName = "ВАЗ / Lada", shortName = "ВАЗ",
            primaryColor = Color(0xFF1B3A6B), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("classic", "Классика (2101–2107)", "📂", 1970, 1988, listOf(
                    EncSystem("wiring", "Электросхема бортовой сети (6V→12V)", "⚡", listOf("Схемы", "Переход 6→12В")),
                    EncSystem("ignition", "Система зажигания", "🔋", listOf("Контактная", "Бесконтактная")),
                    EncSystem("lights", "Освещение и световая сигнализация", "💡", listOf("Схемы фар", "Поворотники")),
                    EncSystem("gen", "Генератор и реле-регулятор", "🔌", listOf("Проверка", "Регулировка")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("ТОП-10 проблем"))
                )),
                EncPlatform("samara", "Самара / Спутник (2108–2115)", "📂", 1984, 2013, listOf(
                    EncSystem("wiring", "Бортовая сеть 12V", "⚡", listOf("Блок предохранителей")),
                    EncSystem("carb_inj", "Карбюратор → Инжектор", "🔋", listOf("Переход", "Разница диагностики")),
                    EncSystem("ecu", "ЭБУ Январь 4/5/Bosch", "🖥️", listOf("Распиновка ЭБУ", "Коды ошибок (P-коды)", "Диагностика")),
                    EncSystem("body", "Электрооборудование кузова", "💡", listOf("Стеклоподъёмники", "ЦЗ")),
                    EncSystem("trouble", "Частые проблемы по электрике", "🛠️", listOf("Генератор", "Стартер", "ЭБУ"))
                )),
                EncPlatform("desyatka", "Десятка / Калина / Приора (2110–2172)", "📂", 1995, 2018, listOf(
                    EncSystem("wiring", "Архитектура бортовой сети", "⚡", listOf("Предохранители", "Масса")),
                    EncSystem("ecu", "ЭБУ (Bosch M7.9.7, Январь 7.2, VS 8.0)", "🖥️", listOf("Расположение и распиновка", "Таблица ошибок OBD-II", "Замена и адаптация")),
                    EncSystem("sensors", "Датчики (ДМРВ, ДТОЖ, ДПДЗ, ДД, ДПКВ)", "🌡️", listOf("Методы проверки мультиметром", "Параметры нормы", "Коды датчиков")),
                    EncSystem("can", "CAN-шина (Приора)", "🔌", listOf("Топология шины", "Устройства на шине")),
                    EncSystem("lights", "Система освещения", "💡", listOf("Биксенон Приора", "DRL")),
                    EncSystem("immo", "Иммобилайзер (VS 8.0, ПИВТ)", "🔐", listOf("Прошивка", "Обучение ключей")),
                    EncSystem("trouble", "ТОП-20 неисправностей", "🛠️", listOf("Генератор", "Форсунки", "Дроссель"))
                )),
                EncPlatform("modern", "Гранта / Веста / XRAY / Нива Travel (2018–н.в.)", "📂", 2018, null, listOf(
                    EncSystem("arch", "Мультиплексная CAN/LIN архитектура", "⚡", listOf("Топология")),
                    EncSystem("ecu", "ЭБУ двигателя (Bosch ME17, Continental SID)", "🖥️", listOf("Диагностический протокол", "Адаптация", "Прошивки")),
                    EncSystem("can_lin", "CAN-шина и LIN-шина", "📡", listOf("Схема топологии", "Узлы: BCM, ABS, Airbag", "Диагностика шины")),
                    EncSystem("immo", "ЭСУД и иммобилайзер", "🔐", listOf("Принцип", "Обучение")),
                    EncSystem("bcm", "Электросистемы кузова (BCM)", "🧠", listOf("Кодирование", "Блокировки")),
                    EncSystem("climate", "Климатическое оборудование", "❄️", listOf("Схема", "Коды")),
                    EncSystem("battery", "АКБ и управление зарядом", "🔋", listOf("IBS-датчик", "Режимы зарядки")),
                    EncSystem("dtc", "Коды ошибок по системам (DTC-каталог)", "❌", listOf("P-коды Веста", "U-коды CAN"))
                ))
            )
        ),

        EncBrand(
            id = "uaz", displayName = "УАЗ", shortName = "УАЗ",
            primaryColor = Color(0xFF006633), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_uaz,
            platforms = listOf(
                EncPlatform("classic_uaz", "Классика (УАЗ-469, Буханка)", "📂", 1972, 2010, listOf(
                    EncSystem("wiring", "Простая бортовая сеть 12V", "⚡", listOf("Жгуты", "Предохранители")),
                    EncSystem("ignition", "Система зажигания", "🔋", listOf("Контактная", "Бесконтактная")),
                    EncSystem("gen", "Генератор / Стартер", "🔌", listOf("Проверка", "Замена")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Проводка", "Реле"))
                )),
                EncPlatform("patriot", "Патриот / Пикап (2005–н.в.)", "📂", 2005, null, listOf(
                    EncSystem("wiring", "Бортовая сеть с OBD-II", "⚡", listOf("Блок реле/предохранителей", "Схемы")),
                    EncSystem("ecu", "ЭБУ двигателя (Bosch / Motronik)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data", "Адаптация дросселя")),
                    EncSystem("4wd", "Система 4WD (Подключаемый передний мост)", "🚗", listOf("Электросхема", "Датчики", "Блокировка")),
                    EncSystem("abs", "ABS (Bosch 8.1)", "🛡️", listOf("Датчики АБС", "Коды")),
                    EncSystem("trouble", "Типовые неисправности Патриота", "🛠️", listOf("ЭБУ", "Проводка кузова", "ABS"))
                ))
            )
        ),

        EncBrand(
            id = "gaz", displayName = "ГАЗ / Газель / Соболь", shortName = "ГАЗ",
            primaryColor = Color(0xFF1A1A1A), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_gaz,
            platforms = listOf(
                EncPlatform("gazelle_old", "Газель 2705 / Соболь (до 2010)", "📂", 1994, 2010, listOf(
                    EncSystem("wiring", "Бортовая сеть 12V / 24V", "⚡", listOf("Дизель 24V", "Бензин 12V")),
                    EncSystem("engine", "Двигатели УМЗ-4216 / ЗМЗ-405", "🖥️", listOf("Карбюраторные версии", "Инжектор Январь")),
                    EncSystem("trouble", "Электрика коммерческого авто", "🛠️", listOf("Стартер", "Генератор", "Освещение"))
                )),
                EncPlatform("gazelle_next", "Газель Next / NN (2013–н.в.)", "📂", 2013, null, listOf(
                    EncSystem("arch", "Современная архитектура", "⚡", listOf("CAN-шина", "Модульная электрика")),
                    EncSystem("ecu", "ЭБУ двигателя (Delphi MT80 / Continental)", "🖥️", listOf("Коды ошибок", "Диагностика OBD-II", "Адаптации")),
                    EncSystem("diesel", "Дизельный двигатель Cummins ISF 2.8", "⛽", listOf("Common Rail", "Коды ошибок дизеля", "Давление Rail")),
                    EncSystem("body", "Электрика кузова и кабины", "💡", listOf("Освещение", "Сигнализация", "ЦЗ")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("ЭБУ Delphi", "Стартер / Генератор"))
                )),
                EncPlatform("sobol", "Соболь / Баргузин", "📂", 1998, null, listOf(
                    EncSystem("wiring", "Электросхема Соболя", "⚡", listOf("Жгуты", "Блок предохранителей")),
                    EncSystem("engine", "Двигатели ЗМЗ-406 / 405", "🖥️", listOf("Инжектор", "Коды ошибок")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Проводка", "Генератор"))
                ))
            )
        ),

        EncBrand(
            id = "moskvich", displayName = "Москвич", shortName = "Москвич",
            primaryColor = Color(0xFF1C2F6E), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("moskvich_3", "Москвич 3 / 3е (2022–н.в.)", "📂", 2022, null, listOf(
                    EncSystem("arch", "Архитектура на базе JAC JS4", "⚡", listOf("CAN-шина", "Блоки управления")),
                    EncSystem("ecu", "ЭБУ двигателя (1.5T / EV)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data")),
                    EncSystem("ev", "Электрическая версия 3е (EV)", "🔌", listOf("Высоковольтная система", "Зарядка AC/DC", "Коды ошибок EV")),
                    EncSystem("trouble", "Типовые неисправности (ранние авто)", "🛠️", listOf("Проводка", "Программное обеспечение"))
                ))
            )
        ),

        // ─── ГЕРМАНИЯ ─────────────────────────────────────────────────────────

        EncBrand(
            id = "volkswagen", displayName = "Volkswagen", shortName = "VW",
            primaryColor = Color(0xFF003399), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_volkswagen,
            platforms = listOf(
                EncPlatform("vag_common", "Общая архитектура VAG", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN-шина: топология", "🌐", listOf("Comfort CAN", "Drive CAN", "Media CAN")),
                    EncSystem("lin", "LIN-шина: устройства", "🔌", listOf("Протокол", "Проверка")),
                    EncSystem("flexray", "FlexRay", "📡", listOf("Новые модели")),
                    EncSystem("blocks", "Номера блоков управления", "🖥️", listOf("Адреса 01–7F")),
                    EncSystem("coding", "Кодирование блоков", "🔧", listOf("Long Coding Helper", "Адаптации")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("Принцип работы", "Адаптация ключей"))
                )),
                EncPlatform("pq35", "Платформа PQ35 (Golf 5/6, Passat B6/B7)", "📂", 2003, 2014, listOf(
                    EncSystem("wiring", "Архитектура электросети", "⚡", listOf("Предохранители", "Массы")),
                    EncSystem("ecu", "ЭБУ двигателя", "🖥️", listOf("Распиновка", "DTC-список (P/U-коды)", "Мессблоки (Measuring Blocks)")),
                    EncSystem("bcm", "BCM (блок комфорта J519 / J393)", "🧠", listOf("Кодирование", "Типовые неисправности")),
                    EncSystem("abs", "ABS / ESP (J104)", "🛡️", listOf("Адаптация", "Датчики")),
                    EncSystem("airbag", "Airbag (J234)", "💺", listOf("Crash data", "Замена пиропатронов")),
                    EncSystem("climate", "Климат", "❄️", listOf("Climatronic vs Climatic", "Адаптация заслонок")),
                    EncSystem("battery", "Управление АКБ", "🔋", listOf("BST", "IBS-датчик"))
                )),
                EncPlatform("mqb", "Платформа MQB (Golf 7/8, Tiguan 2)", "📂", 2012, 2025, listOf(
                    EncSystem("mild_hybrid", "48V мягкий гибрид", "⚡", listOf("MQB evo")),
                    EncSystem("ecu", "ЭБУ (Bosch MG1, Continental SID310)", "🖥️", listOf("DTC", "Специфика")),
                    EncSystem("gateway", "Gateway (J533)", "📡", listOf("Схема подключения шин", "Диагностика через gateway")),
                    EncSystem("bcm", "BCM 2.0 (J519)", "🧠", listOf("Long coding", "Адаптации освещения")),
                    EncSystem("matrix", "Matrix LED (J431)", "💡", listOf("Калибровка", "Коды ошибок")),
                    EncSystem("cameras", "Камеры и ассистенты", "📷", listOf("Front Camera", "ACC")),
                    EncSystem("mib", "MIB2/MIB3", "🔐", listOf("Медиасистема", "Кодирование"))
                )),
                EncPlatform("diag", "VAG-диагностика (VCDS / OBD)", "📂", 2000, 2025, listOf(
                    EncSystem("addresses", "Адреса блоков и их функции", "🔧", listOf("Список адресов")),
                    EncSystem("measuring", "Мессблоки (Live Data)", "📊", listOf("Список полей", "Нормальные значения")),
                    EncSystem("adaptations", "Адаптационные каналы", "⚙️", listOf("Частые настройки")),
                    EncSystem("basic", "Базовые установки", "🔄", listOf("Дроссель", "EGR")),
                    EncSystem("dtc", "Расшифровка кодов ошибок", "❌", listOf("P/C/B/U коды"))
                ))
            )
        ),

        EncBrand(
            id = "mercedes", displayName = "Mercedes-Benz", shortName = "Mercedes",
            primaryColor = Color(0xFF1A1A1A), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_mercedes,
            platforms = listOf(
                EncPlatform("common_mb", "Общая архитектура Mercedes", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN / MOST / FlexRay / Ethernet", "🌐", listOf("Шинная топология", "Gateway SCN")),
                    EncSystem("star", "XENTRY / Star Diagnosis", "🔧", listOf("Подключение", "Guided Functions")),
                    EncSystem("immo", "Иммобилайзер (EZS / DAS)", "🔐", listOf("Программирование", "Обучение ключей")),
                    EncSystem("scn", "SCN-кодирование блоков", "⚙️", listOf("Принцип", "Процедура"))
                )),
                EncPlatform("w205_213", "W205 (C-класс) / W213 (E-класс) 2014–2022", "📂", 2014, 2022, listOf(
                    EncSystem("wiring", "Архитектура сети (CAN FD)", "⚡", listOf("EIS / SCN", "Жгуты кузова")),
                    EncSystem("ecu", "ЭБУ двигателя (OM651/OM654 дизель, M274/M276 бензин)", "🖥️", listOf("Коды ошибок", "Live Data", "Адаптации форсунок")),
                    EncSystem("airmatic", "AIRMATIC (пневмоподвеска)", "💺", listOf("Компрессор", "Клапанный блок", "Коды ошибок", "Диагностика давлений")),
                    EncSystem("mbux", "MBUX / NTG5/NTG6 Медиасистема", "💡", listOf("Кодирование", "Обновления")),
                    EncSystem("abs_esp", "ABS / ESP / PRE-SAFE", "🛡️", listOf("Адаптация", "Замена датчиков")),
                    EncSystem("battery", "Управление АКБ (AGM/EFB)", "🔋", listOf("IBS-датчик", "Регистрация АКБ"))
                )),
                EncPlatform("w167", "W167 (GLE/GLS) / W222 (S-класс)", "📂", 2014, 2025, listOf(
                    EncSystem("wiring", "Многошинная архитектура FlexRay", "⚡", listOf("Топология шин")),
                    EncSystem("magic_body", "MAGIC BODY CONTROL / E-ACTIVE BODY", "💺", listOf("Стереокамера", "Клапаны", "Коды ошибок")),
                    EncSystem("comfort", "Системы комфорта (вентиляция/подогрев)", "❄️", listOf("Управляющий блок", "Диагностика")),
                    EncSystem("dtc", "DTC каталог для W167/W222", "❌", listOf("P/C/B/U коды"))
                )),
                EncPlatform("eq_ev", "EQ серия (EQA, EQB, EQC, EQS)", "📂", 2019, null, listOf(
                    EncSystem("hv", "Высоковольтная система (400V / 800V)", "⚠️", listOf("Меры безопасности", "HV-разъёмы")),
                    EncSystem("battery_ev", "Traction Battery (HV АКБ)", "🔋", listOf("BMS", "Ячейки", "Термоуправление")),
                    EncSystem("charge", "Зарядная система (AC / DC, CCS)", "🔌", listOf("OBC", "DC Fast Charge")),
                    EncSystem("dtc_ev", "EV DTC каталог", "❌", listOf("Коды ошибок EV"))
                ))
            )
        ),

        EncBrand(
            id = "audi", displayName = "Audi", shortName = "Audi",
            primaryColor = Color(0xFFBB0A30), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_audi,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Audi", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN / MOST шины", "🌐", listOf("Drive", "Comfort", "Media", "MOST оптика")),
                    EncSystem("gateway", "Центральный шлюз (J533)", "🖥️", listOf("Диагностика оптики")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("IMMO 4", "IMMO 5")),
                    EncSystem("key", "Advanced Key", "🔑", listOf("MMI-интеграция"))
                )),
                EncPlatform("mlb", "Платформа MLB / MLB Evo", "📂", 2008, 2025, listOf(
                    EncSystem("wiring", "Архитектура 12V + 48V", "⚡", listOf("mild-hybrid")),
                    EncSystem("ecu", "ЭБУ двигателя", "🖥️", listOf("Распиновки", "Коды ошибок", "Адаптации форсунок и дросселя")),
                    EncSystem("flexray", "FlexRay шина", "📡", listOf("Топология", "Диагностика ODIS")),
                    EncSystem("air_susp", "Пневмоподвеска (Air Suspension)", "💺", listOf("Компрессор (J403)", "Клапанный блок", "Коды ошибок", "Давления")),
                    EncSystem("matrix", "Matrix LED / OLED", "💡", listOf("Блок управления J431", "Кодирование", "Диагностика")),
                    EncSystem("bcm", "BCM / BCM2 (J519 / J393)", "🧠", listOf("Охрана", "Комфорт")),
                    EncSystem("climate", "Climatronic 3/4-зонный", "❄️", listOf("Заслонки")),
                    EncSystem("quattro", "quattro (Haldex vs Torsen)", "🚗", listOf("Муфта Haldex", "Адаптации", "Замена масла (электро)")),
                    EncSystem("batt", "Управление АКБ", "🔋", listOf("EFB", "AGM", "IBS J367"))
                )),
                EncPlatform("mmi", "MMI / Виртуальный кокпит", "📂", 2005, 2025, listOf(
                    EncSystem("mmi_gen", "MMI 2G / 3G / 3G+", "🖥️", listOf("Кодирование", "Скрытые меню")),
                    EncSystem("vc", "Virtual Cockpit", "📊", listOf("MIB2", "MIB3")),
                    EncSystem("carplay", "CarPlay / Android Auto", "📱", listOf("Активация"))
                )),
                EncPlatform("odis", "ODIS / VCDS специфика", "📂", 2000, 2025, listOf(
                    EncSystem("guided", "Guided Functions", "🔧", listOf("Сервисные процедуры")),
                    EncSystem("long", "Long Coding Helper", "⚙️", listOf("Audi специфика")),
                    EncSystem("labels", "Label-файлы", "📊", listOf("Мессблоки"))
                ))
            )
        ),

        EncBrand(
            id = "opel", displayName = "Opel", shortName = "Opel",
            primaryColor = Color(0xFFF3E500), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_opel,
            platforms = listOf(
                EncPlatform("delta_opel", "Платформа Delta (Astra J, Zafira C, Insignia)", "📂", 2008, 2020, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Блоки предохранителей", "Жгуты")),
                    EncSystem("ecm", "ECM (Bosch ME17.9, Delco E78)", "🖥️", listOf("Коды ошибок GM/Opel", "Live Data", "Адаптация дросселя")),
                    EncSystem("bcm", "BCM", "🧠", listOf("GDS2 программирование", "Функции")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Адаптация")),
                    EncSystem("climate", "Климат (HVAC)", "❄️", listOf("Самодиагностика", "Коды"))
                )),
                EncPlatform("mokka_corsa", "Mokka / Corsa E/F (2012–2023)", "📂", 2012, 2023, listOf(
                    EncSystem("wiring", "Архитектура сети", "⚡", listOf("GMLAN", "LIN")),
                    EncSystem("ecm", "ECM малообъёмных двигателей", "🖥️", listOf("1.0T / 1.2T ECOTEC", "Коды ошибок")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Электрика", "Датчики"))
                )),
                EncPlatform("diag_opel", "Диагностика Opel (GDS2 / Tech2)", "📂", 2000, 2025, listOf(
                    EncSystem("gds2", "GDS2 — подключение и функции", "🔧", listOf("SPS программирование")),
                    EncSystem("live", "Live Data параметры", "📊", listOf("По блокам")),
                    EncSystem("dtc", "DTC каталог Opel/GM", "❌", listOf("P/B/C/U коды"))
                ))
            )
        ),

        // ─── ЯПОНИЯ ────────────────────────────────────────────────────────────

        EncBrand(
            id = "toyota", displayName = "Toyota", shortName = "Toyota",
            primaryColor = Color(0xFFEB0A1E), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_toyota,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Toyota", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN-шина: Multi-CAN", "🌐", listOf("SFI", "Body", "DLC")),
                    EncSystem("proto", "Диагностический протокол", "📡", listOf("ISO 15765", "KWP2000")),
                    EncSystem("blocks", "Блоки управления и их ID", "🖥️", listOf("Список ECU")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("Типы (IMMO, Smart Entry)", "Адаптация ключей"))
                )),
                EncPlatform("ice", "Традиционные ДВС", "📂", 2000, 2025, listOf(
                    EncSystem("wiring", "Бортовая сеть 12V / 14V", "⚡", listOf("Реле", "Предохранители")),
                    EncSystem("ecu", "ЭБУ двигателя", "🖥️", listOf("Коды ошибок (Toyota DTC)", "Live Data", "Распиновки")),
                    EncSystem("sensors", "Датчики", "🌡️", listOf("Осциллограммы", "Коды отказов датчиков")),
                    EncSystem("abs", "ABS / VSC / TRAC", "🛡️", listOf("Адаптация", "Zero Point Calibration")),
                    EncSystem("airbag", "SRS Airbag", "💺", listOf("Считывание кодов скрепкой")),
                    EncSystem("smart_key", "Smart Key System", "🔑", listOf("Схема работы", "Мёртвые зоны антенны", "Диагностика")),
                    EncSystem("battery", "Управление АКБ", "🔋", listOf("Датчики тока"))
                )),
                EncPlatform("hybrid", "Hybrid Synergy Drive", "📂", 2003, 2025, listOf(
                    EncSystem("safety", "ВЫСОКОВОЛЬТНАЯ СИСТЕМА", "⚠️", listOf("Меры безопасности", "Отключение")),
                    EncSystem("hv_battery", "Высоковольтная АКБ", "🔋", listOf("Модули и ячейки", "Система охлаждения", "Блок управления (BMU)", "Коды ошибок (P0A0x)", "Балансировка")),
                    EncSystem("inverter", "Инвертор / Конвертер", "⚡", listOf("Принцип работы", "Коды ошибок инвертора", "Диагностика изоляции")),
                    EncSystem("charge", "Зарядная система", "🔌", listOf("12V / HV", "Проверка")),
                    EncSystem("hv_ecu", "HV ECU", "🖥️", listOf("Коды ошибок HV", "Мессблоки (SOC, температуры)", "Сервисный режим (SERVICE MODE)")),
                    EncSystem("safety_sys", "Система безопасности HV", "🛡️", listOf("SMR-реле", "Interlock"))
                )),
                EncPlatform("techstream", "Диагностика Toyota", "📂", 2000, 2025, listOf(
                    EncSystem("setup", "Подключение и настройка", "🔧", listOf("Techstream", "G-scan")),
                    EncSystem("live", "Параметры Live Data", "📊", listOf("Эталонные значения")),
                    EncSystem("active", "Активные тесты", "⚙️", listOf("Actuator Tests")),
                    EncSystem("dtc", "DTC каталог", "❌", listOf("General", "HV-специфика"))
                ))
            )
        ),

        EncBrand(
            id = "nissan", displayName = "Nissan", shortName = "Nissan",
            primaryColor = Color(0xFFC3002F), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_nissan,
            platforms = listOf(
                EncPlatform("common_nissan", "Общая архитектура Nissan", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN-шина Nissan (AV-CAN, EV-CAN)", "🌐", listOf("Топология шин", "Адреса блоков")),
                    EncSystem("consult", "Протокол CONSULT-III / CONSULT-III+", "📡", listOf("Подключение", "Диагностика")),
                    EncSystem("immo", "Иммобилайзер (NATS)", "🔐", listOf("Программирование ключей", "Сброс"))
                )),
                EncPlatform("cd_platform", "C/D-платформа (Qashqai J10/J11, X-Trail T30/T31/T32)", "📂", 2001, 2020, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Предохранители", "Схемы")),
                    EncSystem("ecu", "ECM (MEC32, MEC37)", "🖥️", listOf("Коды ошибок", "Live Data", "Адаптации")),
                    EncSystem("abs_vdc", "ABS / VDC / TCS", "🛡️", listOf("Датчики АБС", "Коды ошибок")),
                    EncSystem("cvt", "CVT вариатор (RE0F09A / Jatco)", "⚙️", listOf("Коды ошибок CVT", "Процедура адаптации", "Замена масла")),
                    EncSystem("4wd", "ALL MODE 4x4-i (Nissan)", "🚗", listOf("Электромуфта", "Коды ошибок AWD")),
                    EncSystem("climate", "Климат HVAC", "❄️", listOf("Самодиагностика", "Коды клапанов"))
                )),
                EncPlatform("patrol_navara", "Patrol Y61/Y62 / Navara D40/D23", "📂", 1998, 2025, listOf(
                    EncSystem("wiring", "Электрика внедорожника", "⚡", listOf("24V дизель", "Двойная АКБ")),
                    EncSystem("ecm_diesel", "ECM дизельного двигателя YD25/ZD30/V9X", "🖥️", listOf("Common Rail", "Коды ошибок дизеля", "EGR специфика")),
                    EncSystem("4wd_part", "Part-Time 4WD (Patrol Y61)", "🚗", listOf("Электросхема", "Муфты")),
                    EncSystem("trouble", "Типовые проблемы", "🛠️", listOf("ZD30 болячки", "Электрика Y62"))
                )),
                EncPlatform("leaf_ev", "Nissan Leaf / Ariya (EV)", "📂", 2010, null, listOf(
                    EncSystem("hv", "Высоковольтная система (360-400V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery_ev", "HV Battery (LMO / NMC ячейки)", "🔋", listOf("Охлаждение (пассивное/активное)", "BMS", "Коды ошибок P0A0x")),
                    EncSystem("charge", "CHAdeMO / Type 2 зарядка", "🔌", listOf("Протокол CHAdeMO", "OBC", "Быстрая зарядка")),
                    EncSystem("dtc_ev", "EV DTC каталог", "❌", listOf("Коды ошибок EV"))
                ))
            )
        ),

        EncBrand(
            id = "honda", displayName = "Honda", shortName = "Honda",
            primaryColor = Color(0xFFCC0000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_honda,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Honda", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN шина (F-CAN, B-CAN)", "🌐", listOf("Топология F-CAN", "Топология B-CAN")),
                    EncSystem("hds", "Протокол Honda HDS", "📡", listOf("ISO 15765-4")),
                    EncSystem("blocks", "Блоки", "🖥️", listOf("ECM/PCM", "TCM", "ABS/VSA", "SRS", "BCM", "EPS")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("Тип 1 / Тип 2", "Регистрация ключей"))
                )),
                EncPlatform("civic", "Civic / Accord / CR-V", "📂", 2006, 2025, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("MICU")),
                    EncSystem("pcm", "ECM/PCM", "🖥️", listOf("Коды ошибок Honda", "Флэш-программирование")),
                    EncSystem("vtec", "VTEC / i-VTEC", "🌡️", listOf("Соленоид VTEC", "OCV", "Коды ошибок (P2646)")),
                    EncSystem("cvt", "CVT вариатор", "⚙️", listOf("Электронное управление TCM", "Коды CVT (P17xx)", "Процедура замены ATF")),
                    EncSystem("vsa", "VSA", "🛡️", listOf("Vehicle Stability Assist")),
                    EncSystem("airbag", "SRS Airbag", "💺", listOf("Honda-специфика")),
                    EncSystem("climate", "Климат", "❄️", listOf("Dual Zone"))
                )),
                EncPlatform("fit", "Fit / Jazz / HR-V", "📂", 2001, 2025, listOf(
                    EncSystem("wiring", "Упрощённая архитектура", "⚡", listOf("Особенности")),
                    EncSystem("ecm", "ECM", "🖥️", listOf("PGM-FI управление")),
                    EncSystem("4wd", "Real Time AWD", "🚗", listOf("Электромуфта")),
                    EncSystem("trouble", "Типовые проблемы", "🛠️", listOf("Электрика"))
                )),
                EncPlatform("hybrid", "IMA / e:HEV Hybrid", "📂", 2000, 2025, listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("100–200V IMA", "250V+ e:HEV")),
                    EncSystem("batt", "IMA Battery", "🔋", listOf("Модули и балансировка", "Коды ошибок (P13xx)")),
                    EncSystem("pcu", "IMA Motor / PCU", "⚡", listOf("Принцип", "Диагностика PCU")),
                    EncSystem("ehev", "e:HEV", "🔌", listOf("Отличие от IMA", "Специфические коды"))
                )),
                EncPlatform("diag", "Honda Diagnostics", "📂", 2000, 2025, listOf(
                    EncSystem("hds", "Honda HDS", "🔧", listOf("Настройка")),
                    EncSystem("live", "Live Data", "📊", listOf("Параметры по блокам")),
                    EncSystem("adapt", "Сервисные процедуры", "⚙️", listOf("Обучение дроссельной заслонки", "Адаптация руля EPS", "Сброс Maintenance Minder")),
                    EncSystem("dtc", "Honda DTC каталог", "❌", listOf("VTEC / CVT специфика"))
                ))
            )
        ),

        EncBrand(
            id = "mazda", displayName = "Mazda", shortName = "Mazda",
            primaryColor = Color(0xFF101010), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_mazda,
            platforms = listOf(
                EncPlatform("skyactiv", "SKYACTIV (Mazda3/6/CX-5, 2012–2022)", "📂", 2012, 2022, listOf(
                    EncSystem("wiring", "Бортовая сеть SKYACTIV", "⚡", listOf("MS-CAN", "HS-CAN", "LIN")),
                    EncSystem("ecu", "PCM (SKYACTIV-G / D / X)", "🖥️", listOf("Коды ошибок Mazda", "Live Data", "Адаптация форсунок")),
                    EncSystem("ids", "Mazda IDS / MGSS диагностика", "🔧", listOf("Подключение", "Aktive тесты")),
                    EncSystem("awd", "AWD i-ACTIV (CX-5 / CX-9)", "🚗", listOf("Электромуфта", "Коды ошибок AWD")),
                    EncSystem("abs", "ABS / DSC / TCS", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("climate", "Климат (Auto A/C)", "❄️", listOf("Самодиагностика", "Заслонки"))
                )),
                EncPlatform("mx5_rx8", "MX-5 / RX-8 (спорт)", "📂", 1998, 2012, listOf(
                    EncSystem("wiring", "Электрика спортивного авто", "⚡", listOf("Минимальная масса жгутов")),
                    EncSystem("renesis", "Двигатель RENESIS (RX-8, роторный)", "🖥️", listOf("Коды ошибок P0301-P0304", "Масло в двигателе", "Компрессия роторов")),
                    EncSystem("trouble", "Болячки RX-8 по электрике", "🛠️", listOf("ЭБУ", "Форсунки OMP", "Прогрев"))
                )),
                EncPlatform("cx90_lrg", "CX-90 / CX-60 (2022–н.в.) PHEV", "📂", 2022, null, listOf(
                    EncSystem("phev", "PHEV система (Mazda e-SKYACTIV)", "⚡", listOf("Гибридный блок", "Коды ошибок PHEV")),
                    EncSystem("hv", "Высоковольтная система (355V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("charge", "Зарядная система", "🔌", listOf("AC Type 2", "OBC")),
                    EncSystem("dtc", "DTC каталог PHEV", "❌", listOf("Коды ошибок гибрида"))
                ))
            )
        ),

        EncBrand(
            id = "subaru", displayName = "Subaru", shortName = "Subaru",
            primaryColor = Color(0xFF00458C), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_subaru,
            platforms = listOf(
                EncPlatform("sgp", "Платформа SGP (Impreza / Forester / Outback / XV, 2011–2022)", "📂", 2011, 2022, listOf(
                    EncSystem("wiring", "Симметричный полный привод AWD", "⚡", listOf("Архитектура сети", "Жгуты")),
                    EncSystem("ecu", "ECM (DENSO/Hitachi, FA20/FB20)", "🖥️", listOf("Коды ошибок Subaru OBD-II", "Live Data", "Адаптации форсунок")),
                    EncSystem("vdc", "VDC / ABS (Vehicle Dynamics Control)", "🛡️", listOf("Датчики боковой G", "Коды ошибок")),
                    EncSystem("eyesight", "EyeSight (стереокамера ADAS)", "📷", listOf("Калибровка камеры", "Коды ошибок EyeSight")),
                    EncSystem("cvt", "Lineartronic CVT (TR580 / TR690)", "⚙️", listOf("Коды CVT", "Адаптация", "Масло CVT")),
                    EncSystem("climate", "Климат (Dual Zone Auto)", "❄️", listOf("Диагностика", "Коды заслонок"))
                )),
                EncPlatform("sti_wrx", "WRX STi / BRZ (спорт)", "📂", 2003, 2025, listOf(
                    EncSystem("wiring", "Электрика спортивного авто", "⚡", listOf("Доп. жгуты STi")),
                    EncSystem("ecu_turbo", "ECM EJ20/EJ25 Turbo", "🖥️", listOf("Специфика наддува", "Коды knock sensor", "Boost управление")),
                    EncSystem("dccd", "DCCD (Driver Controlled Centre Diff)", "🚗", listOf("Электронное управление", "Коды ошибок"))
                )),
                EncPlatform("solterra_ev", "Solterra / BZ4X (EV, совм. Toyota)", "📂", 2022, null, listOf(
                    EncSystem("hv", "Высоковольтная система (355V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "HV Battery", "🔋", listOf("BMS", "Thermal Management")),
                    EncSystem("charge", "Зарядная система CCS/CHAdeMO", "🔌", listOf("OBC", "DC Fast Charge")),
                    EncSystem("dtc", "DTC EV каталог", "❌", listOf("Коды EV"))
                ))
            )
        ),

        EncBrand(
            id = "suzuki", displayName = "Suzuki", shortName = "Suzuki",
            primaryColor = Color(0xFFE31837), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_suzuki,
            platforms = listOf(
                EncPlatform("vitara_sx4", "Vitara / SX4 S-Cross / Swift (2005–2023)", "📂", 2005, 2023, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Блоки предохранителей", "Схемы")),
                    EncSystem("ecu", "ECM (Suzuki SDT-II диагностика)", "🖥️", listOf("Коды ошибок", "Live Data")),
                    EncSystem("allgrip", "AllGrip AWD (4WD Select)", "🚗", listOf("Режимы Auto/Snow/Sport/Lock", "Электромуфта", "Коды AWD")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Калибровка")),
                    EncSystem("climate", "Климат", "❄️", listOf("Ручной / Авто кондиционер"))
                )),
                EncPlatform("jimny", "Jimny (JB74, 2018–н.в.)", "📂", 2018, null, listOf(
                    EncSystem("wiring", "Электрика внедорожника", "⚡", listOf("Схемы", "Предохранители")),
                    EncSystem("ecu", "ECM (1.5L K15B)", "🖥️", listOf("Коды ошибок", "Специфика")),
                    EncSystem("4wd_jimny", "Suzuki-Matic 4WD (Part-Time)", "🚗", listOf("Электроуправление раздатки", "Коды"))
                )),
                EncPlatform("diag_suzuki", "Диагностика Suzuki (SDT-II)", "📂", 2005, 2025, listOf(
                    EncSystem("sdt", "Suzuki SDT-II — подключение", "🔧", listOf("Процедура")),
                    EncSystem("dtc", "DTC каталог Suzuki", "❌", listOf("P-коды", "Специфика"))
                ))
            )
        ),

        EncBrand(
            id = "lexus", displayName = "Lexus", shortName = "Lexus",
            primaryColor = Color(0xFF202020), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_lexus,
            platforms = listOf(
                EncPlatform("gal", "Платформа GA-L (IS/GS/LS/LX, до 2022)", "📂", 2006, 2022, listOf(
                    EncSystem("can", "Multi-CAN архитектура Lexus", "🌐", listOf("Drive CAN", "Body CAN", "AV CAN")),
                    EncSystem("ecu", "ECM (Denso)", "🖥️", listOf("Коды ошибок Toyota/Lexus", "Live Data", "Адаптация форсунок")),
                    EncSystem("air_susp", "Adaptive Variable Suspension / Air Suspension", "💺", listOf("Коды ошибок", "Калибровка высоты")),
                    EncSystem("ls_hvac", "Климат (4-зонный)", "❄️", listOf("Управляющий блок", "Коды заслонок")),
                    EncSystem("smart_key", "Smart Key / Smart Entry", "🔑", listOf("Антенны", "Программирование ключей"))
                )),
                EncPlatform("tnga", "Платформа TNGA (UX/NX/RX/LX, 2018–н.в.)", "📂", 2018, null, listOf(
                    EncSystem("arch", "Архитектура TNGA (Ethernet + CAN)", "⚡", listOf("Топология")),
                    EncSystem("ecu_tnga", "ECM TNGA (2.0T / 3.5 V6 / Diesel)", "🖥️", listOf("Коды ошибок", "TNGA-специфика")),
                    EncSystem("adas", "Lexus Safety System + (LSS+)", "📷", listOf("Предупреждение о столкновении", "Калибровка камеры"))
                )),
                EncPlatform("hybrid_lexus", "Hybrid (RX450h, LS600h, LC500h)", "📂", 2005, null, listOf(
                    EncSystem("hv", "Высоковольтная система (288-650V)", "⚠️", listOf("Меры безопасности HV", "Interlock")),
                    EncSystem("hv_battery", "HV Battery (NiMH / Li-ion)", "🔋", listOf("Коды ошибок P0A0x", "Балансировка ячеек")),
                    EncSystem("inverter", "Инвертор / PCU", "⚡", listOf("MG1 / MG2", "Диагностика изоляции")),
                    EncSystem("dtc_hv", "HV DTC каталог", "❌", listOf("Коды гибридных систем"))
                )),
                EncPlatform("diag_lexus", "Диагностика Lexus (Techstream)", "📂", 2000, 2025, listOf(
                    EncSystem("ts_setup", "Techstream — подключение", "🔧", listOf("Настройка", "Лицензия")),
                    EncSystem("live", "Live Data Lexus", "📊", listOf("Эталонные значения")),
                    EncSystem("dtc", "DTC каталог Lexus", "❌", listOf("OBD-II + Lexus-специфика"))
                ))
            )
        ),

        EncBrand(
            id = "infiniti", displayName = "Infiniti", shortName = "Infiniti",
            primaryColor = Color(0xFF000000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_infiniti,
            platforms = listOf(
                EncPlatform("fm", "Платформа FM (Q50/Q60/QX70, 2013–2023)", "📂", 2013, 2023, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("CAN шина", "Жгуты")),
                    EncSystem("ecu", "ECM (VQ37VHR / VR30DDTT)", "🖥️", listOf("Коды ошибок", "Live Data CONSULT-III")),
                    EncSystem("aws", "All-Wheel Steering (DAS — Direct Adaptive Steering)", "🚗", listOf("Электрорулевое управление", "Калибровка")),
                    EncSystem("abs_vdc", "ABS / VDC", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("climate", "Климат (Dual/Triple zone)", "❄️", listOf("Диагностика", "Сервисный режим"))
                )),
                EncPlatform("cmf", "Платформа CMF (QX50/QX55, 2019–н.в.)", "📂", 2019, null, listOf(
                    EncSystem("ecu", "ECM (VC-Turbo KR15DDT — двигатель переменной степени сжатия)", "🖥️", listOf("Специфика VC-Turbo", "Коды ошибок")),
                    EncSystem("propi", "ProPilot Assist (ADAS)", "📷", listOf("Калибровка камеры", "Коды ошибок ProPilot")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("VC-Turbo специфика"))
                )),
                EncPlatform("diag_infiniti", "Диагностика Infiniti (CONSULT-III)", "📂", 2005, 2025, listOf(
                    EncSystem("consult", "CONSULT-III подключение", "🔧", listOf("Настройка", "Блоки")),
                    EncSystem("dtc", "DTC каталог Infiniti", "❌", listOf("P-коды", "U-коды CAN"))
                ))
            )
        ),

        EncBrand(
            id = "acura", displayName = "Acura", shortName = "Acura",
            primaryColor = Color(0xFF000000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_acura,
            platforms = listOf(
                EncPlatform("mac", "Платформа MAC (TLX/MDX, 2020–н.в.)", "📂", 2020, null, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("F-CAN", "B-CAN", "Ethernet")),
                    EncSystem("ecu", "PCM (2.0T VTEC / 3.5 V6 / Type S 3.0TT)", "🖥️", listOf("Коды ошибок Honda/Acura", "Live Data HDS")),
                    EncSystem("sh_awd", "SH-AWD (Super Handling AWD)", "🚗", listOf("Электромоторы на задней оси", "Векторизация момента", "Коды ошибок AWD")),
                    EncSystem("adas", "AcuraWatch (EyeSight-базовый)", "📷", listOf("Калибровка", "Коды")),
                    EncSystem("climate", "Климат (Tri-Zone)", "❄️", listOf("Диагностика", "Коды заслонок"))
                )),
                EncPlatform("rdx_ilx", "RDX / ILX / TL (2013–2020)", "📂", 2013, 2020, listOf(
                    EncSystem("ecu", "ECM Honda-based", "🖥️", listOf("Коды ошибок Honda HDS", "Адаптации")),
                    EncSystem("awd", "AWD SH-AWD старая версия", "🚗", listOf("Электромуфта", "Коды")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("VTEC", "CVT"))
                ))
            )
        ),

        EncBrand(
            id = "mitsubishi", displayName = "Mitsubishi", shortName = "Mitsu",
            primaryColor = Color(0xFFED1A3B), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_mitsubishi,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Mitsubishi", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN-шина", "🌐", listOf("M-CAN", "Body CAN")),
                    EncSystem("mut", "Протокол MUT-II / MUT-III", "📡", listOf("Фирменный сканер")),
                    EncSystem("blocks", "Блоки", "🖥️", listOf("ECM", "TCM", "ABS", "SRS", "BCM", "EPS")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("MIT-Immo", "Регистрация ключей"))
                )),
                EncPlatform("outlander", "Outlander / ASX / Eclipse Cross", "📂", 2003, 2025, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("ETACS")),
                    EncSystem("ecm", "ECM (MPI / GDI)", "🖥️", listOf("Коды ошибок", "Live Data")),
                    EncSystem("awc", "AWD / S-AWC", "🚗", listOf("Электромуфта", "Диагностика")),
                    EncSystem("hvac", "HVAC", "❄️", listOf("Manual", "Digital")),
                    EncSystem("kessy", "Keyless Entry", "🔑", listOf("KESSY"))
                )),
                EncPlatform("lancer", "Lancer / Evolution", "📂", 1991, 2017, listOf(
                    EncSystem("evo", "Специфика Evo", "⚡", listOf("Жгуты проводки")),
                    EncSystem("ecm", "ECM (4B11 Evo X)", "🖥️", listOf("Наддув", "Коды")),
                    EncSystem("acd", "ACD / AYC", "🚗", listOf("Гидравлический ACD", "Коды ошибок", "Процедура обслуживания")),
                    EncSystem("abs", "ABS с интеграцией AYC", "🛡️", listOf("Особенности"))
                )),
                EncPlatform("l200_pajero", "L200 / Pajero / Montero", "📂", 1996, 2025, listOf(
                    EncSystem("wiring", "24V система (дизель)", "⚡", listOf("Pajero")),
                    EncSystem("ecm", "ECM дизельных двигателей (4M41, 4D56)", "🖥️", listOf("CRDI", "Диагностика давления Rail")),
                    EncSystem("trailer", "Электрика прицепа", "🔌", listOf("Распиновка")),
                    EncSystem("4wd", "Super Select 4WD II", "🚗", listOf("Электронное управление"))
                )),
                EncPlatform("phev", "Outlander PHEV", "📂", 2013, null, listOf(
                    EncSystem("hv", "Высоковольтная система (300V+)", "⚠️", listOf("300V+")),
                    EncSystem("battery", "HV-аккумулятор", "🔋", listOf("Li-ion 12 kWh")),
                    EncSystem("motors", "Электромоторы", "⚡", listOf("Front / Rear MG")),
                    EncSystem("charge", "Зарядная система", "🔌", listOf("CHAdeMO", "Type 1")),
                    EncSystem("ev_ecu", "EV-ECU", "🖥️", listOf("Управление энергопотоками"))
                ))
            )
        ),

        EncBrand(
            id = "datsun", displayName = "Datsun", shortName = "Datsun",
            primaryColor = Color(0xFF004B87), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_datsun,
            platforms = listOf(
                EncPlatform("on_do", "on-DO / mi-DO (платформа ВАЗ Гранта)", "📂", 2014, 2022, listOf(
                    EncSystem("wiring", "Бортовая сеть (на базе ВАЗ 2190)", "⚡", listOf("Жгуты", "Предохранители")),
                    EncSystem("ecu", "ЭБУ двигателя (Bosch ME17)", "🖥️", listOf("Коды ошибок OBD-II", "Адаптации")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Электрика", "ЭБУ"))
                ))
            )
        ),

        // ─── США ───────────────────────────────────────────────────────────────

        EncBrand(
            id = "ford", displayName = "Ford", shortName = "Ford",
            primaryColor = Color(0xFF003478), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_ford,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Ford", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN HS / MS / LS шины", "🌐", listOf("3-шинная топология")),
                    EncSystem("obd", "OBD-II протоколы", "📡", listOf("CAN ISO 15765")),
                    EncSystem("modules", "Модульные блоки управления", "🖥️", listOf("PCM", "BCM", "GEM", "PATS")),
                    EncSystem("pats", "PATS (Иммобилайзер)", "🔐", listOf("Поколения PATS", "Программирование ключей"))
                )),
                EncPlatform("c1", "Платформа C1 / C2 (Focus 2, C-Max, Kuga 1)", "📂", 2004, 2015, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Схемы GEM", "Болячки приборки")),
                    EncSystem("pcm", "PCM", "🖥️", listOf("Коды ошибок OBD-II", "Параметры PID (Live Data)")),
                    EncSystem("gem", "GEM", "🧠", listOf("Освещение, стеклоподъёмники", "Программирование конфигурации")),
                    EncSystem("abs", "ABS / RSC", "🛡️", listOf("Roll Stability Control")),
                    EncSystem("climate", "Климат-контроль", "❄️", listOf("Manual", "Auto", "Самодиагностика"))
                )),
                EncPlatform("c2_fwd", "Платформа C2 / FWD (Focus 3, Mondeo 5)", "📂", 2011, 2023, listOf(
                    EncSystem("arch", "Архитектура сети", "⚡", listOf("HS-CAN", "MS-CAN", "LIN")),
                    EncSystem("pcm", "PCM (EcoBoost)", "🖥️", listOf("Специфика EcoBoost", "Типовые коды (P0299)")),
                    EncSystem("bcm", "BCM", "🧠", listOf("Программирование параметров", "Блокировки")),
                    EncSystem("sync", "SYNC / MyFord Touch", "💡", listOf("Медиасистема", "Обновления")),
                    EncSystem("cam", "Камеры", "📷", listOf("Задняя", "360°")),
                    EncSystem("bms", "Управление АКБ (BMS)", "🔋", listOf("Сброс BMS"))
                )),
                EncPlatform("f_series", "F-Series / Ranger / Transit", "📂", 2000, 2025, listOf(
                    EncSystem("batt", "Dual Battery System", "⚡", listOf("Transit специфика")),
                    EncSystem("pcm", "PCM V8 / V6", "🖥️", listOf("EcoBoost V6", "PowerStroke")),
                    EncSystem("trans", "TorqShift АКПП", "⚙️", listOf("Электрическое управление")),
                    EncSystem("trailer", "Электрика прицепа", "🔌", listOf("7-pin Ford", "Модуль прицепа")),
                    EncSystem("safety", "Системы безопасности коммерческих", "🛡️", listOf("Специфика"))
                )),
                EncPlatform("ev", "Ford EV / Hybrid (Mach-E, F-150 Lightning)", "📂", 2019, null, listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("Mach-E", "F-150 Lightning")),
                    EncSystem("battery", "Traction Battery", "🔋", listOf("LFP / NMC ячейки")),
                    EncSystem("charge", "Зарядные системы", "⚡", listOf("AC / DC", "CCS")),
                    EncSystem("becm", "BECM", "🖥️", listOf("Battery Energy Control Module"))
                ))
            )
        ),

        EncBrand(
            id = "chevrolet", displayName = "Chevrolet", shortName = "Chevy",
            primaryColor = Color(0xFFD4AF37), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_chevrolet,
            platforms = listOf(
                EncPlatform("gm_common", "Общая архитектура GM", "📂", 2000, 2025, listOf(
                    EncSystem("gmlan", "GMLAN HS / LS", "🌐", listOf("GM CAN")),
                    EncSystem("proto", "Протоколы", "📡", listOf("Class 2", "GMLAN")),
                    EncSystem("blocks", "GM-блоки", "🖥️", listOf("ECM", "BCM", "TCM", "EBCM", "SCM")),
                    EncSystem("passlock", "PassLock / VATS", "🔐", listOf("GM Passlock 3", "Процедура сброса"))
                )),
                EncPlatform("delta", "Платформа Delta (Cruze / Astra J)", "📂", 2009, 2020, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Блоки предохранителей")),
                    EncSystem("ecm", "ECM", "🖥️", listOf("Коды ошибок GM DTC", "PID Data List", "Сервисные процедуры")),
                    EncSystem("bcm", "BCM", "🧠", listOf("Программирование", "Управляемые функции")),
                    EncSystem("ebcm", "EBCM", "🛡️", listOf("Тормозная система")),
                    EncSystem("climate", "Климат", "❄️", listOf("HVAC модуль")),
                    EncSystem("rke", "Remote Keyless Entry", "🔑", listOf("RKE"))
                )),
                EncPlatform("gamma", "Платформа Gamma (Spark, Sonic / Aveo 2)", "📂", 2010, 2022, listOf(
                    EncSystem("wiring", "Упрощённая архитектура", "⚡", listOf("Особенности")),
                    EncSystem("ecm", "ECM", "🖥️", listOf("Малообъёмные двигатели")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Электрика"))
                )),
                EncPlatform("gmt", "GMT (Silverado / Tahoe / Suburban)", "📂", 2000, 2025, listOf(
                    EncSystem("wiring", "Двойная АКБ / Генератор", "⚡", listOf("Heavy duty")),
                    EncSystem("ecm", "ECM V8", "🖥️", listOf("AFM / DFM", "Диагностика масляной системы AFM")),
                    EncSystem("trans", "Hydra-Matic АКПП", "⚙️", listOf("Электрическое управление", "Коды ошибок")),
                    EncSystem("trailer", "Электрика прицепа", "🔌", listOf("7-pin GM")),
                    EncSystem("drl", "Daytime Running Lights", "💡", listOf("GM DRL"))
                )),
                EncPlatform("ev_chevy", "EV / Hybrid (Bolt EV, Volt)", "📂", 2011, null, listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("Основы")),
                    EncSystem("batt", "Ultium Battery", "🔋", listOf("Thermal Management System")),
                    EncSystem("charge", "Зарядка", "⚡", listOf("J1772", "CCS Combo")),
                    EncSystem("pim", "BECM / PIM", "🖥️", listOf("Power Inverter Module"))
                ))
            )
        ),

        EncBrand(
            id = "dodge", displayName = "Dodge", shortName = "Dodge",
            primaryColor = Color(0xFFE01825), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_dodge,
            platforms = listOf(
                EncPlatform("fca_ld", "Платформа FCA-LD (Charger / Challenger / Durango)", "📂", 2005, 2023, listOf(
                    EncSystem("wiring", "Бортовая сеть CAN-C / CAN-B", "⚡", listOf("Блок TIPM", "Жгуты")),
                    EncSystem("pcm", "PCM (5.7 HEMI / 6.4 HEMI / 3.6 Pentastar)", "🖥️", listOf("Коды ошибок Dodge wiTECH", "Live Data", "Цилиндры HEMI MDS")),
                    EncSystem("tipm", "TIPM (Totally Integrated Power Module)", "🧠", listOf("Программирование TIPM", "Типовые отказы (стартер)", "Коды ошибок TIPM")),
                    EncSystem("abs_esb", "ABS / ESC / TCS", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("trans", "ZF 8HP / NAG1 АКПП", "⚙️", listOf("Адаптация TCM", "Коды ошибок трансмиссии")),
                    EncSystem("climate", "Климат (HVAC)", "❄️", listOf("Dual zone", "Коды"))
                )),
                EncPlatform("ram", "RAM 1500 / 2500 / 3500", "📂", 2009, 2025, listOf(
                    EncSystem("wiring", "Электрика пикапа (Dual Battery / Heavy Duty)", "⚡", listOf("Генератор 220А", "Прицепной разъём")),
                    EncSystem("pcm", "PCM HEMI / Cummins дизель", "🖥️", listOf("Коды ошибок", "DEF (AdBlue) система")),
                    EncSystem("airsus", "Air Ride Suspension (пневмоподвеска RAM 1500)", "💺", listOf("Компрессор", "Коды ошибок"))
                )),
                EncPlatform("diag_dodge", "Диагностика Dodge/Chrysler (wiTECH)", "📂", 2005, 2025, listOf(
                    EncSystem("witech", "wiTECH 2.0 — подключение", "🔧", listOf("Процедура")),
                    EncSystem("dtc", "DTC каталог FCA", "❌", listOf("P/B/C/U коды"))
                ))
            )
        ),

        EncBrand(
            id = "chrysler", displayName = "Chrysler", shortName = "Chrysler",
            primaryColor = Color(0xFF000000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_chrysler,
            platforms = listOf(
                EncPlatform("fca_chrysler", "Платформа FCA (300C / Pacifica / Town&Country)", "📂", 2004, 2023, listOf(
                    EncSystem("wiring", "Бортовая сеть (CAN-C / CAN-B)", "⚡", listOf("Жгуты", "TIPM")),
                    EncSystem("pcm", "PCM (3.6 Pentastar / 5.7 HEMI)", "🖥️", listOf("Коды ошибок wiTECH", "Live Data")),
                    EncSystem("tipm", "TIPM (Totally Integrated Power Module)", "🧠", listOf("Программирование", "Типовые отказы")),
                    EncSystem("uconnect", "Uconnect медиасистема", "💡", listOf("Программирование", "Обновления")),
                    EncSystem("trouble", "Типовые неисправности Chrysler", "🛠️", listOf("TIPM проблемы", "Запуск двигателя"))
                ))
            )
        ),

        EncBrand(
            id = "cadillac", displayName = "Cadillac", shortName = "Cadillac",
            primaryColor = Color(0xFF000000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_cadillac,
            platforms = listOf(
                EncPlatform("vss_t", "Платформа GM VSS-T / T1 (XT5/CT6/Escalade)", "📂", 2015, 2025, listOf(
                    EncSystem("wiring", "Архитектура бортовой сети", "⚡", listOf("GMLAN FD", "Ethernet Backbone")),
                    EncSystem("ecm", "ECM (LT1/LT4/Duramax дизель)", "🖥️", listOf("Коды ошибок GDS2", "Live Data")),
                    EncSystem("air_mag", "Magnetic Ride Control + Air Suspension", "💺", listOf("Magneride клапаны", "Коды ошибок", "Калибровка высоты")),
                    EncSystem("cue", "CUE (Cadillac User Experience) медиасистема", "💡", listOf("Программирование", "Обновления")),
                    EncSystem("abs_esb", "ABS / StabiliTrak", "🛡️", listOf("Датчики", "Калибровка")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Электрика", "MRC"))
                )),
                EncPlatform("lyriq_ev", "LYRIQ / CELESTIQ (EV, Ultium)", "📂", 2022, null, listOf(
                    EncSystem("hv", "Высоковольтная система Ultium (400V/800V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "Ultium Battery", "🔋", listOf("BMS", "Thermal")),
                    EncSystem("charge", "Зарядка (CCS / DC Fast)", "🔌", listOf("OBC", "Быстрая зарядка")),
                    EncSystem("dtc_ev", "EV DTC каталог", "❌", listOf("Коды EV"))
                ))
            )
        ),

        // ─── КИТАЙ ─────────────────────────────────────────────────────────────

        EncBrand(
            id = "chery", displayName = "Chery", shortName = "Chery",
            primaryColor = Color(0xFF003366), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_chery,
            platforms = listOf(
                EncPlatform("m1x", "Платформа M1X (Tiggo 4/7/8, Arrizo, 2017–н.в.)", "📂", 2017, null, listOf(
                    EncSystem("wiring", "Бортовая сеть CHERY-CAN", "⚡", listOf("HS-CAN", "MS-CAN")),
                    EncSystem("ecu", "ECM (1.5T SQRA5FE / 2.0T)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data Chery Tool Kit")),
                    EncSystem("abs", "ABS / ESP (Bosch 9.3)", "🛡️", listOf("Датчики", "Адаптация")),
                    EncSystem("awd", "AWD (Tiggo 7/8 Pro)", "🚗", listOf("Электромуфта Haldex", "Коды ошибок")),
                    EncSystem("climate", "Климат", "❄️", listOf("Авто кондиционер", "Диагностика")),
                    EncSystem("trouble", "Типовые неисправности Chery", "🛠️", listOf("ЭБУ", "CAN-шина"))
                )),
                EncPlatform("chery_ev", "Chery EV / PHEV (Arrizo 5e, Tiggo 8 Pro e+)", "📂", 2020, null, listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "HV Battery", "🔋", listOf("BMS", "Thermal")),
                    EncSystem("charge", "Зарядная система", "🔌", listOf("Type 2 AC", "DC GB/T")),
                    EncSystem("dtc", "DTC каталог EV/PHEV", "❌", listOf("Коды ошибок"))
                ))
            )
        ),

        EncBrand(
            id = "omoda", displayName = "Omoda", shortName = "Omoda",
            primaryColor = Color(0xFF000000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_omoda,
            platforms = listOf(
                EncPlatform("omoda_c5", "Omoda C5 / C9 (платформа Chery, 2022–н.в.)", "📂", 2022, null, listOf(
                    EncSystem("wiring", "Бортовая сеть (на базе Chery M1X)", "⚡", listOf("CAN-шина", "LIN-шина")),
                    EncSystem("ecu", "ECM (1.6T SQRE4T15)", "🖥️", listOf("Коды ошибок OBD-II", "Диагностика Chery Tool Kit")),
                    EncSystem("awd", "AWD (C9 версия)", "🚗", listOf("Электромуфта", "Коды ошибок AWD")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("ЭБУ", "CAN-шина"))
                ))
            )
        ),

        EncBrand(
            id = "geely", displayName = "Geely", shortName = "Geely",
            primaryColor = Color(0xFF00438A), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_geely,
            platforms = listOf(
                EncPlatform("bma_cma", "Платформа BMA/CMA (Atlas/Coolray/Monjaro, 2016–н.в.)", "📂", 2016, null, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("CAN-шина", "LIN")),
                    EncSystem("ecu", "ECM (1.5T 4G15T / 1.8T JLY-4G18TD)", "🖥️", listOf("Коды ошибок", "Live Data GDiag")),
                    EncSystem("abs", "ABS / ESP (Bosch/Mando)", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("awd", "AWD 4WD (Monjaro / Atlas Pro)", "🚗", listOf("Haldex-based муфта", "Коды")),
                    EncSystem("phev", "PHEV / HEV (Monjaro HEV)", "🔌", listOf("Гибридный блок", "Коды PHEV")),
                    EncSystem("trouble", "Типовые неисправности Geely", "🛠️", listOf("ЭБУ", "Датчики"))
                ))
            )
        ),

        EncBrand(
            id = "changan", displayName = "Changan", shortName = "Changan",
            primaryColor = Color(0xFF005BAC), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_changan,
            platforms = listOf(
                EncPlatform("epa", "Платформа EPA0/EPA2 (CS75 / UNI-T / UNI-V, 2019–н.в.)", "📂", 2019, null, listOf(
                    EncSystem("wiring", "Бортовая сеть CAN/LIN", "⚡", listOf("Архитектура", "Блоки")),
                    EncSystem("ecu", "ECM (1.5T Blue Whale / 2.0T Blue Whale)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("awd", "AWD (CS75 Plus, UNI-T 4WD)", "🚗", listOf("Электромуфта", "Режимы 4WD")),
                    EncSystem("trouble", "Типовые неисправности Changan", "🛠️", listOf("ЭБУ", "Шина CAN"))
                )),
                EncPlatform("ev_changan", "Changan EV (Deepal / Avatr)", "📂", 2021, null, listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "HV Battery", "🔋", listOf("BMS", "Thermal")),
                    EncSystem("charge", "Зарядка (GB/T / CCS)", "🔌", listOf("OBC", "DC")),
                    EncSystem("dtc", "DTC EV каталог", "❌", listOf("Коды ошибок"))
                ))
            )
        ),

        EncBrand(
            id = "byd", displayName = "BYD", shortName = "BYD",
            primaryColor = Color(0xFFDE0E19), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_byd,
            platforms = listOf(
                EncPlatform("e_platform3", "e-Platform 3.0 (Atto 3 / Seal / Dolphin, 2021–н.в.)", "📂", 2021, null, listOf(
                    EncSystem("arch", "Архитектура e-Platform 3.0 (800V + 400V)", "⚡", listOf("Отличие от предыдущих платформ", "Контроллеры")),
                    EncSystem("hv", "Высоковольтная система (400V/800V)", "⚠️", listOf("Меры безопасности", "HV-разъёмы")),
                    EncSystem("blade_bat", "Blade Battery (LFP ячейки)", "🔋", listOf("Конструкция", "BMS", "Thermal Management", "Коды ошибок P0A0x")),
                    EncSystem("charge", "Зарядка (GB/T AC+DC / CCS)", "🔌", listOf("OBC 11kW", "DC 100-150kW", "V2L", "V2G")),
                    EncSystem("dm_hybrid", "DM-i / DM-p PHEV (Han, Tang, Song Plus)", "🔌", listOf("Архитектура DM-i", "Двойная коробка", "Коды ошибок PHEV")),
                    EncSystem("dilink", "DiLink медиасистема", "💡", listOf("Android-based", "OTA обновления")),
                    EncSystem("dtc", "DTC каталог BYD", "❌", listOf("EV коды", "PHEV коды"))
                ))
            )
        ),

        EncBrand(
            id = "lifan", displayName = "Lifan", shortName = "Lifan",
            primaryColor = Color(0xFF0053A0), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_lifan,
            platforms = listOf(
                EncPlatform("x60_myway", "X60 / X70 / Myway (2012–2020)", "📂", 2012, 2020, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Предохранители", "Схемы")),
                    EncSystem("ecu", "ECM (1.8L LFB479Q / 2.0L LFB4B9Q)", "🖥️", listOf("Коды ошибок OBD-II", "Диагностика ELM327")),
                    EncSystem("abs", "ABS (Bosch 8.1 / TRW)", "🛡️", listOf("Датчики", "Типовые коды")),
                    EncSystem("trouble", "Типовые неисправности Lifan", "🛠️", listOf("Электрика кузова", "ЭБУ", "Датчики"))
                ))
            )
        ),

        EncBrand(
            id = "dongfeng", displayName = "Dongfeng", shortName = "Dongfeng",
            primaryColor = Color(0xFF003087), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("s30_ax", "S30 / AX4 / AX7 (2010–2020)", "📂", 2010, 2020, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Предохранители", "Схемы")),
                    EncSystem("ecu", "ECM (1.5T / 2.0L Renault-based)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("trouble", "Типовые неисправности Dongfeng", "🛠️", listOf("Электрика кузова", "ЭБУ"))
                ))
            )
        ),

        EncBrand(
            id = "jac", displayName = "JAC", shortName = "JAC",
            primaryColor = Color(0xFF004C97), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("jac_s3_s7", "JAC S3 / S7 / J4 / J7 (2015–н.в.)", "📂", 2015, null, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("CAN-шина", "Предохранители")),
                    EncSystem("ecu", "ECM (1.5T / 2.0T JAC-base)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("trouble", "Типовые неисправности JAC", "🛠️", listOf("Электрика", "ЭБУ"))
                )),
                EncPlatform("jac_ev", "JAC EV (iEV6E / iEV7S)", "📂", 2018, null, listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "HV Battery", "🔋", listOf("BMS", "Thermal")),
                    EncSystem("charge", "Зарядка GB/T", "🔌", listOf("AC / DC")),
                    EncSystem("dtc", "DTC EV каталог JAC", "❌", listOf("Коды ошибок"))
                ))
            )
        ),

        EncBrand(
            id = "jetour", displayName = "Jetour", shortName = "Jetour",
            primaryColor = Color(0xFF1A1A3E), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("jetour_x70_x90", "Jetour X70 / X90 / Dashing (платформа Chery, 2018–н.в.)", "📂", 2018, null, listOf(
                    EncSystem("wiring", "Бортовая сеть (аналог Chery M1X)", "⚡", listOf("CAN-шина", "LIN")),
                    EncSystem("ecu", "ECM (1.5T / 2.0T Chery-based)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data")),
                    EncSystem("awd", "AWD (X90 4WD)", "🚗", listOf("Электромуфта", "Коды AWD")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("trouble", "Типовые неисправности Jetour", "🛠️", listOf("ЭБУ", "CAN-шина"))
                ))
            )
        ),

        // ─── КОРЕЯ ─────────────────────────────────────────────────────────────

        EncBrand(
            id = "hyundai", displayName = "Hyundai", shortName = "Hyundai",
            primaryColor = Color(0xFF002C5F), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_hyundai,
            platforms = listOf(
                EncPlatform("common_hkmc", "Общая архитектура Hyundai / KIA (HKMC)", "📂", 2000, 2025, listOf(
                    EncSystem("can", "CAN C-CAN / B-CAN / M-CAN", "🌐", listOf("Топология шин", "Адреса блоков")),
                    EncSystem("gds", "Диагностика GDS2 / GDS Mobile", "📡", listOf("Подключение", "Функции")),
                    EncSystem("immo", "Иммобилайзер (SMARTRA)", "🔐", listOf("Программирование ключей", "Адаптация")),
                    EncSystem("scc", "Smart Cruise Control (ADAS)", "📷", listOf("Калибровка радара", "Коды ошибок"))
                )),
                EncPlatform("i_gmp", "Платформа i-GMP (Sonata NF8/DN8, Tucson NX4/TL, Santa Fe TM, 2019–н.в.)", "📂", 2019, null, listOf(
                    EncSystem("wiring", "Бортовая сеть i-GMP", "⚡", listOf("Многоблочная архитектура", "Предохранители")),
                    EncSystem("ecu", "ECM (G4NC 2.0 / G4KH 1.6T / G4KJ 2.4)", "🖥️", listOf("Коды ошибок Hyundai OBD-II", "Live Data GDS2")),
                    EncSystem("dcm", "DCM (Door Control Module)", "🧠", listOf("Управление дверьми", "Программирование")),
                    EncSystem("abs_esc", "ABS / ESC / TCS", "🛡️", listOf("Датчики", "Адаптация")),
                    EncSystem("cvt_dct", "8-ступ. АКПП / DCT (7-ст.)", "⚙️", listOf("Коды ошибок трансмиссии", "Адаптация TCM")),
                    EncSystem("climate", "Климат (Dual Zone Auto)", "❄️", listOf("Диагностика", "Самодиагностика"))
                )),
                EncPlatform("e_gmp", "Платформа E-GMP (IONIQ 5/6, EV6)", "📂", 2021, null, listOf(
                    EncSystem("hv", "Высоковольтная система (800V)", "⚠️", listOf("Меры безопасности 800V")),
                    EncSystem("battery_ev", "800V NCM Battery", "🔋", listOf("BMS", "Thermal Management", "Коды P0A0x")),
                    EncSystem("charge", "Зарядка 800V (CCS 350kW, V2L)", "🔌", listOf("Единая зарядная система", "V2L функция")),
                    EncSystem("awd_ev", "AWD EV (передний + задний мотор)", "🚗", listOf("E-LSD", "Векторизация")),
                    EncSystem("dtc_ev", "DTC EV каталог", "❌", listOf("Коды ошибок EV/PHEV"))
                ))
            )
        ),

        EncBrand(
            id = "kia", displayName = "Kia", shortName = "Kia",
            primaryColor = Color(0xFF05141F), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_kia,
            platforms = listOf(
                EncPlatform("kia_igmp", "Платформа i-GMP (Sportage NQ5, Sorento MQ4, K5/K8, 2019–н.в.)", "📂", 2019, null, listOf(
                    EncSystem("wiring", "Бортовая сеть KIA (общая с Hyundai)", "⚡", listOf("CAN C-CAN", "B-CAN", "LIN")),
                    EncSystem("ecu", "ECM (T-GDi 1.6 / 2.5 / G4FW 2.0 HEV)", "🖥️", listOf("Коды ошибок KIA OBD-II", "Live Data GDS2")),
                    EncSystem("abs_esc", "ABS / ESC", "🛡️", listOf("Датчики", "Адаптация")),
                    EncSystem("awd", "AWD HTRAC (Sorento, Sportage 4WD)", "🚗", listOf("Электромуфта", "Коды")),
                    EncSystem("hev_phev", "HEV / PHEV (Sportage HEV, Sorento PHEV)", "🔌", listOf("Архитектура гибрида", "Коды ошибок"))
                )),
                EncPlatform("kia_egmp", "Платформа E-GMP (EV6 / EV9)", "📂", 2021, null, listOf(
                    EncSystem("hv", "Высоковольтная система (800V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "NCM Battery (E-GMP)", "🔋", listOf("BMS", "800V Thermal")),
                    EncSystem("charge", "Зарядка 800V CCS", "🔌", listOf("Rapid Charging System", "V2L")),
                    EncSystem("dtc", "DTC EV каталог KIA", "❌", listOf("Коды EV"))
                )),
                EncPlatform("stinger", "Stinger (2017–2023, FR-платформа)", "📂", 2017, 2023, listOf(
                    EncSystem("ecu", "ECM (G4KH 2.0T / Lambda 3.3TT)", "🖥️", listOf("Коды ошибок", "Специфика 3.3TT")),
                    EncSystem("abs", "ABS / ESC (Brembo тормоза)", "🛡️", listOf("Датчики", "Коды"))
                ))
            )
        ),

        EncBrand(
            id = "daewoo", displayName = "Daewoo / Ravon", shortName = "Daewoo",
            primaryColor = Color(0xFF003087), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("nexia_matiz", "Nexia / Matiz / Lanos (1996–2012)", "📂", 1996, 2012, listOf(
                    EncSystem("wiring", "Простая бортовая сеть 12V", "⚡", listOf("Схемы", "Предохранители")),
                    EncSystem("ecu", "ECM Delphi / Simtec (A15SMS / A16DMS)", "🖥️", listOf("Коды ошибок OBD-II", "Карбюратор vs инжектор")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Электрика", "ЭБУ", "Форсунки"))
                )),
                EncPlatform("ravon_gentra", "Ravon R4 / Nexia R3 / Gentra (Узбекистан, 2015–н.в.)", "📂", 2015, null, listOf(
                    EncSystem("wiring", "Архитектура (GM-based)", "⚡", listOf("CAN", "Предохранители")),
                    EncSystem("ecu", "ECM (1.5 F15S / 1.6 F16D3)", "🖥️", listOf("Коды ошибок OBD-II", "Live Data")),
                    EncSystem("trouble", "Типовые неисправности Ravon", "🛠️", listOf("Электрика", "ЭБУ"))
                ))
            )
        ),

        // ─── ФРАНЦИЯ ───────────────────────────────────────────────────────────

        EncBrand(
            id = "renault", displayName = "Renault", shortName = "Renault",
            primaryColor = Color(0xFFFFCC00), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_renault,
            platforms = listOf(
                EncPlatform("cmf_b", "Платформа CMF-B (Logan/Sandero/Duster/Kaptur, 2012–н.в.)", "📂", 2012, null, listOf(
                    EncSystem("wiring", "Бортовая сеть (CAN / LIN)", "⚡", listOf("Мультиплекс", "Предохранители")),
                    EncSystem("ecu", "ECM (H4M 1.6 / H5Ft 1.3T / K9K дизель)", "🖥️", listOf("Коды ошибок CAN Clip", "Live Data")),
                    EncSystem("ucm", "UCM (Universal Comfort Module)", "🧠", listOf("Управление кузовом", "Кодирование")),
                    EncSystem("abs_esp", "ABS / ESP (Bosch 8.0)", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("climate", "Климат", "❄️", listOf("Ручной / Авто", "Диагностика CAN Clip"))
                )),
                EncPlatform("cmf", "Платформа CMF (Megane/Laguna/Talisman, 2015–н.в.)", "📂", 2015, null, listOf(
                    EncSystem("wiring", "CAN FD архитектура (новые модели)", "⚡", listOf("Топология", "Жгуты")),
                    EncSystem("ecu", "ECM (1.3 TCe/1.8T TCe)", "🖥️", listOf("Коды ошибок", "Live Data")),
                    EncSystem("ef1", "АКПП EDC (EF1 / EDC7)", "⚙️", listOf("Коды ошибок трансмиссии", "Адаптация")),
                    EncSystem("trouble", "Типовые неисправности Renault", "🛠️", listOf("ЭБУ", "CAN-шина"))
                )),
                EncPlatform("zoe_ev", "Zoe / Megane E-Tech EV", "📂", 2013, null, listOf(
                    EncSystem("hv", "Высоковольтная система (300-400V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "HV Battery (Z.E. / Lithium)", "🔋", listOf("BMS", "Thermal", "Замена батареи")),
                    EncSystem("charge", "Зарядка Chameleon / CCS", "🔌", listOf("AC 22kW зарядка", "DC")),
                    EncSystem("dtc", "EV DTC каталог", "❌", listOf("Коды ошибок EV"))
                ))
            )
        ),

        EncBrand(
            id = "peugeot", displayName = "Peugeot", shortName = "Peugeot",
            primaryColor = Color(0xFF002554), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_peugeot,
            platforms = listOf(
                EncPlatform("emp2", "Платформа EMP2 (308/3008/5008/508, 2013–н.в.)", "📂", 2013, null, listOf(
                    EncSystem("wiring", "Бортовая сеть PSA (CAN / LIN / Ethernet)", "⚡", listOf("BSI блок", "Мультиплекс")),
                    EncSystem("ecu", "ECM (1.2 PureTech / 1.6 THP / 2.0 BlueHDi)", "🖥️", listOf("Коды ошибок DiagBox", "Live Data")),
                    EncSystem("bsi", "BSI (Built-in Systems Interface)", "🧠", listOf("Программирование BSI", "Типовые отказы", "Коды ошибок")),
                    EncSystem("abs_esp", "ABS / ESP (Bosch)", "🛡️", listOf("Датчики", "Адаптация")),
                    EncSystem("climate", "Климат (Auto A/C)", "❄️", listOf("Диагностика DiagBox", "Коды")),
                    EncSystem("trouble", "Типовые неисправности PSA/Peugeot", "🛠️", listOf("BSI глюки", "ЭБУ", "CAN-шина"))
                )),
                EncPlatform("e_208_2008", "e-208 / e-2008 (EV, 2020–н.в.)", "📂", 2020, null, listOf(
                    EncSystem("hv", "Высоковольтная система (400V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "HV Battery (50kWh)", "🔋", listOf("BMS", "Thermal")),
                    EncSystem("charge", "Зарядка AC 11kW / DC 100kW CCS", "🔌", listOf("OBC", "DC Fast")),
                    EncSystem("dtc", "EV DTC каталог Peugeot", "❌", listOf("Коды ошибок EV"))
                ))
            )
        ),

        EncBrand(
            id = "citroen", displayName = "Citroen", shortName = "Citroen",
            primaryColor = Color(0xFFDA291C), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_citroen,
            platforms = listOf(
                EncPlatform("emp2_citroen", "Платформа EMP2 (C4/C5/Berlingo, общая с PSA)", "📂", 2009, null, listOf(
                    EncSystem("wiring", "Бортовая сеть PSA (BSI-центральный)", "⚡", listOf("CAN", "LIN", "BSI")),
                    EncSystem("ecu", "ECM (1.6 THP / 1.2 PureTech / HDi дизель)", "🖥️", listOf("Коды ошибок DiagBox", "Live Data")),
                    EncSystem("bsi", "BSI (Built-in Systems Interface)", "🧠", listOf("Типовые отказы", "Программирование", "Сброс BSI")),
                    EncSystem("hds_ds", "Гидропневматика DS / Citroën C5 (Hydractive)", "💺", listOf("Коды ошибок", "Давление сфер", "Замена сфер")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("trouble", "Типовые неисправности Citroën", "🛠️", listOf("BSI", "Гидропневматика", "ЭБУ"))
                ))
            )
        ),

        // ─── ИТАЛИЯ ────────────────────────────────────────────────────────────

        EncBrand(
            id = "fiat", displayName = "Fiat", shortName = "Fiat",
            primaryColor = Color(0xFFE01825), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_fiat,
            platforms = listOf(
                EncPlatform("sgma", "Платформа SGMA / Small (500/Punto/Tipo/Doblo, 2005–н.в.)", "📂", 2005, null, listOf(
                    EncSystem("wiring", "Бортовая сеть FCA (BSM / BCM)", "⚡", listOf("CAN", "LIN", "Жгуты")),
                    EncSystem("ecu", "ECM (1.0 Firefly / 1.3 Multijet / 1.6 Multijet)", "🖥️", listOf("Коды ошибок wiTECH / ExaminerPRO", "Live Data")),
                    EncSystem("blue_me", "Blue&Me медиасистема", "💡", listOf("Диагностика", "Обновления")),
                    EncSystem("multijet", "Дизель Multijet (Common Rail)", "⛽", listOf("Давление Rail", "Коды ошибок дизеля", "EGR система")),
                    EncSystem("trouble", "Типовые неисправности Fiat", "🛠️", listOf("BSM глюки", "Stilo/Punto болячки", "ЭБУ"))
                )),
                EncPlatform("ducato", "Fiat Ducato / Iveco Daily (коммерческий)", "📂", 1994, null, listOf(
                    EncSystem("wiring", "Бортовая сеть коммерческого авто", "⚡", listOf("24V версия", "Схемы")),
                    EncSystem("ecu_diesel", "ECM Multijet дизель (2.0/2.3/3.0 Multijet)", "🖥️", listOf("Коды ошибок", "Давление Rail")),
                    EncSystem("trouble", "Типовые проблемы Ducato", "🛠️", listOf("Электрика", "EGR"))
                ))
            )
        ),

        EncBrand(
            id = "alfa_romeo", displayName = "Alfa Romeo", shortName = "Alfa Romeo",
            primaryColor = Color(0xFF9A0000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_alfa_romeo,
            platforms = listOf(
                EncPlatform("giorgio", "Платформа Giorgio (Giulia/Stelvio, 2016–н.в.)", "📂", 2016, null, listOf(
                    EncSystem("wiring", "Бортовая сеть FCA Premium (CAN FD)", "⚡", listOf("Ethernet backbone", "Топология шин")),
                    EncSystem("ecu", "ECM (2.0T GME 280/4C 147 kW / 2.9 Biturbo)", "🖥️", listOf("Коды ошибок Alfa wiTECH", "Live Data", "Адаптации")),
                    EncSystem("qv_sss", "DNA Switch + Alfa IFS (активная подвеска QV)", "💺", listOf("Режимы DNA", "Коды ошибок", "Калибровка")),
                    EncSystem("q4", "Q4 AWD (Stelvio/Giulia Q4)", "🚗", listOf("Электромуфта PTU", "Коды ошибок")),
                    EncSystem("abs_darr", "ABS / ESC / Brembo Carbon-Ceramic", "🛡️", listOf("Специфика", "Адаптация")),
                    EncSystem("trouble", "Типовые неисправности Alfa Romeo", "🛠️", listOf("Электрика", "Трансмиссия", "Подвеска"))
                ))
            )
        ),

        EncBrand(
            id = "iveco", displayName = "Iveco", shortName = "Iveco",
            primaryColor = Color(0xFF003DA5), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("daily", "Iveco Daily (Euro 5/6, 2006–н.в.)", "📂", 2006, null, listOf(
                    EncSystem("wiring", "Бортовая сеть коммерческого авто", "⚡", listOf("24V / 12V версии", "Блок предохранителей")),
                    EncSystem("ecu", "ECM FPT F1C / N45 / F1A дизель", "🖥️", listOf("Коды ошибок (грузовые)", "Давление Common Rail")),
                    EncSystem("adblue", "SCR / AdBlue (Мочевинная система Euro 6)", "⛽", listOf("Датчик NOx", "Дозатор AdBlue", "Коды ошибок SCR")),
                    EncSystem("gearbox", "Электроника трансмиссии (EuroCargo АКПП)", "⚙️", listOf("ZF AutoShift", "Коды ошибок")),
                    EncSystem("trouble", "Типовые неисправности Iveco Daily", "🛠️", listOf("SCR AdBlue", "EGR", "Электрика кузова"))
                ))
            )
        ),

        // ─── ШВЕЦИЯ ────────────────────────────────────────────────────────────

        EncBrand(
            id = "volvo", displayName = "Volvo", shortName = "Volvo",
            primaryColor = Color(0xFF003057), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_volvo,
            platforms = listOf(
                EncPlatform("spa", "Платформа SPA (XC60/V60/S60/XC90, 2014–н.в.)", "📂", 2014, null, listOf(
                    EncSystem("can", "CAN шина Volvo (HS-CAN / LIN / FlexRay)", "🌐", listOf("Топология", "Gateway")),
                    EncSystem("vida", "Диагностика VIDA / DiCE 2", "🔧", listOf("Подключение", "Процедуры")),
                    EncSystem("ecu", "ECM (Drive-E 2.0T/2.0D двигатели)", "🖥️", listOf("Коды ошибок Volvo", "Live Data", "Адаптация форсунок")),
                    EncSystem("air_susp_v", "Пневмоподвеска (XC90 XC60)", "💺", listOf("Компрессор", "Клапанный блок", "Коды ошибок", "Калибровка")),
                    EncSystem("city_safe", "City Safety / Pilot Assist (ADAS)", "📷", listOf("Калибровка камеры", "Коды ошибок")),
                    EncSystem("battery_v", "Управление АКБ (AGM / EFB)", "🔋", listOf("IBS-датчик", "Регистрация АКБ")),
                    EncSystem("trouble", "Типовые неисправности Volvo", "🛠️", listOf("ЭБУ", "Пневма", "DTM"))
                )),
                EncPlatform("spa2_ev", "Платформа SPA2 / CMA EV (XC40/C40/EX90)", "📂", 2020, null, listOf(
                    EncSystem("hv", "Высоковольтная система (400V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery_ev", "HV Battery (78/82 kWh)", "🔋", listOf("BMS", "Thermal", "Коды P0A0x")),
                    EncSystem("charge", "Зарядка CCS (AC 11kW / DC 150kW)", "🔌", listOf("OBC", "DC Fast")),
                    EncSystem("dtc_ev", "EV DTC каталог Volvo", "❌", listOf("Коды EV"))
                ))
            )
        ),

        // ─── ЧЕХИЯ ─────────────────────────────────────────────────────────────

        EncBrand(
            id = "skoda", displayName = "Skoda", shortName = "Skoda",
            primaryColor = Color(0xFF4BA82E), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_skoda,
            platforms = listOf(
                EncPlatform("mqb_skoda", "Платформа MQB (Octavia A8/A7, Superb B8, Kodiaq, 2012–н.в.)", "📂", 2012, null, listOf(
                    EncSystem("can", "VAG CAN-архитектура (общая с VW)", "🌐", listOf("Comfort CAN", "Drive CAN")),
                    EncSystem("vcds", "Диагностика VCDS / ODIS", "🔧", listOf("Кодирование Skoda", "Guided Functions")),
                    EncSystem("ecu", "ECM (1.0 TSI EA211 / 1.5 TSI / 2.0 TSI / 2.0 TDI)", "🖥️", listOf("Коды ошибок VAG", "Мессблоки", "Адаптации")),
                    EncSystem("bcm", "BCM J519 / KESSY", "🧠", listOf("Кодирование", "Keyless")),
                    EncSystem("dsg", "DSG коробка (DQ200 / DQ381 / DQ500)", "⚙️", listOf("Коды ошибок DSG", "Адаптация", "Замена масла")),
                    EncSystem("abs_esp", "ABS / ESP", "🛡️", listOf("Датчики", "Адаптация")),
                    EncSystem("trouble", "Типовые неисправности Skoda", "🛠️", listOf("ЭБУ", "DSG", "Тепловые зазоры"))
                )),
                EncPlatform("enyaq_ev", "Enyaq iV (EV, MEB-платформа)", "📂", 2021, null, listOf(
                    EncSystem("hv", "Высоковольтная система (400V MEB)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "MEB Battery (58/77 kWh)", "🔋", listOf("BMS", "Thermal")),
                    EncSystem("charge", "Зарядка CCS2 / AC Type 2", "🔌", listOf("OBC 11kW", "DC 125kW")),
                    EncSystem("dtc", "EV DTC каталог Skoda/VW", "❌", listOf("Коды MEB"))
                ))
            )
        ),

        // ─── ИНДИЯ ─────────────────────────────────────────────────────────────

        EncBrand(
            id = "tata", displayName = "Tata", shortName = "Tata",
            primaryColor = Color(0xFF0E3D99), secondaryColor = Color(0xFFFFFFFF),
            logoResId = null,
            platforms = listOf(
                EncPlatform("nexon_harrier", "Nexon / Harrier / Safari (OMEGARC / ALFA-ARC, 2017–н.в.)", "📂", 2017, null, listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("CAN-шина", "Предохранители")),
                    EncSystem("ecu", "ECM (1.2T Revotron / 2.0L Kryotec дизель)", "🖥️", listOf("Коды ошибок OBD-II (SEAD диагностика)", "Live Data")),
                    EncSystem("abs", "ABS / ESP", "🛡️", listOf("Датчики", "Коды")),
                    EncSystem("climate", "Климат", "❄️", listOf("Авто A/C", "Диагностика")),
                    EncSystem("trouble", "Типовые неисправности Tata", "🛠️", listOf("Электрика", "ЭБУ"))
                )),
                EncPlatform("nexon_ev", "Nexon EV / Tiago EV (2020–н.в.)", "📂", 2020, null, listOf(
                    EncSystem("hv", "Высоковольтная система (309V)", "⚠️", listOf("Меры безопасности")),
                    EncSystem("battery", "HV Battery (30.2 / 40.5 kWh)", "🔋", listOf("BMS", "Thermal")),
                    EncSystem("charge", "Зарядка AC Type 2 / DC CCS2", "🔌", listOf("OBC", "DC Fast")),
                    EncSystem("dtc", "EV DTC каталог Tata", "❌", listOf("Коды ошибок EV"))
                ))
            )
        )

    ) // close brandsFlat list

    val countries: List<EncCountry> = listOf(
        EncCountry("russia", "Россия", "🇷🇺", brandsFlat.filter { it.id in listOf("vaz", "uaz", "gaz", "moskvich") }),
        EncCountry("germany", "Германия", "🇩🇪", brandsFlat.filter { it.id in listOf("volkswagen", "mercedes", "audi", "opel") }),
        EncCountry("japan", "Япония", "🇯🇵", brandsFlat.filter { it.id in listOf("toyota", "nissan", "honda", "mazda", "subaru", "suzuki", "lexus", "infiniti", "acura", "mitsubishi", "datsun") }),
        EncCountry("usa", "США", "🇺🇸", brandsFlat.filter { it.id in listOf("ford", "chevrolet", "dodge", "chrysler", "cadillac") }),
        EncCountry("china", "Китай", "🇨🇳", brandsFlat.filter { it.id in listOf("chery", "omoda", "geely", "changan", "byd", "lifan", "dongfeng", "jac", "jetour") }),
        EncCountry("korea", "Корея", "🇰🇷", brandsFlat.filter { it.id in listOf("hyundai", "kia", "daewoo") }),
        EncCountry("france", "Франция", "🇫🇷", brandsFlat.filter { it.id in listOf("renault", "peugeot", "citroen") }),
        EncCountry("italy", "Италия", "🇮🇹", brandsFlat.filter { it.id in listOf("fiat", "alfa_romeo", "iveco") }),
        EncCountry("sweden", "Швеция", "🇸🇪", brandsFlat.filter { it.id in listOf("volvo") }),
        EncCountry("czech", "Чехия", "🇨🇿", brandsFlat.filter { it.id in listOf("skoda") }),
        EncCountry("india", "Индия", "🇮🇳", brandsFlat.filter { it.id in listOf("tata") })
    )

    val brands: List<EncBrand> get() = brandsFlat
}
