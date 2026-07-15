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
    val logoResId: Int,
    val platforms: List<EncPlatform>
)

data class EncPlatform(
    val id: String,
    val displayName: String,
    val icon: String,
    val systems: List<EncSystem>
)

data class EncSystem(
    val id: String,
    val displayName: String,
    val icon: String,
    val subsystems: List<String>
)

object EncyclopediaCatalog {

    val countries: List<EncCountry> = listOf(
        // Россия
        EncCountry(
            id = "russia",
            displayName = "Россия",
            flagEmoji = "🇷🇺",
            brands = listOf(
        // ВАЗ (LADA)
        EncBrand(
            id = "vaz", displayName = "ВАЗ (LADA)", shortName = "ВАЗ",
            primaryColor = Color(0xFFCC0000), secondaryColor = Color(0xFF1A1A2E),
            logoResId = R.drawable.img_logo_vaz,
            platforms = listOf(
                EncPlatform("classic", "Классика (2101–2107)", "📂", listOf(
                    EncSystem("wiring", "Электросхема бортовой сети (6V → 12V)", "⚡", listOf("Схемы", "Типовые проблемы")),
                    EncSystem("ignition", "Система зажигания", "🔋", listOf("Контактное", "Бесконтактное", "Регулировка")),
                    EncSystem("lighting", "Освещение и сигнализация", "💡", listOf("Фары", "Задние фонари", "Реле поворотов")),
                    EncSystem("generator", "Генератор и реле-регулятор", "🔌", listOf("Проверка", "Схема подключения")),
                    EncSystem("troubleshooting", "Типовые неисправности", "🛠️", listOf("Поиск замыканий", "Утечка тока"))
                )),
                EncPlatform("samara", "Самара / Спутник (2108–2115)", "📂", listOf(
                    EncSystem("power_net", "Бортовая сеть 12V", "⚡", listOf("Блок предохранителей", "Схема проводки")),
                    EncSystem("carb_inj", "Карбюратор → Инжектор", "🔋", listOf("Отличия проводок", "Датчики")),
                    EncSystem("ecu", "ЭБУ Январь 4 / 5 / Bosch", "🖥️", listOf("Распиновка ЭБУ", "Коды ошибок (P-коды)", "Процедура диагностики")),
                    EncSystem("body", "Электрооборудование кузова", "💡", listOf("Стеклоподъемники", "Замки дверей")),
                    EncSystem("troubleshooting", "Частые проблемы", "🛠️", listOf("Массы", "Монтажный блок"))
                )),
                EncPlatform("priora", "Десятка / Калина / Приора (2110–2172)", "📂", listOf(
                    EncSystem("arch", "Архитектура бортовой сети", "⚡", listOf("Схема", "Отличия моделей")),
                    EncSystem("ecu", "ЭБУ (Bosch M7.9.7, Январь 7.2, VS 8.0)", "🖥️", listOf("Расположение и распиновка", "Таблица ошибок OBD-II", "Замена и адаптация")),
                    EncSystem("sensors", "Датчики", "🌡️", listOf("Методы проверки мультиметром", "Параметры нормы", "Коды ошибок датчиков")),
                    EncSystem("can", "CAN-шина (Приора)", "🔌", listOf("Топология шины", "Устройства на шине")),
                    EncSystem("lighting", "Система освещения", "💡", listOf("Лампы", "Реле")),
                    EncSystem("immo", "Иммобилайзер (VS 8.0, ПИВТ)", "🔐", listOf("Обучение", "Сброс")),
                    EncSystem("troubleshooting", "ТОП-20 неисправностей", "🛠️", listOf("Популярные болячки"))
                )),
                EncPlatform("vesta", "Гранта / Веста / XRAY / Niva", "📂", listOf(
                    EncSystem("can_lin", "Мультиплексная CAN/LIN архитектура", "⚡", listOf("Топология", "Отличия", "Узлы")),
                    EncSystem("ecu", "ЭБУ двигателя (Bosch ME17, Continental)", "🖥️", listOf("Диагностический протокол", "Адаптация и базовые установки", "Прошивки")),
                    EncSystem("bus_diag", "CAN-шина и LIN-шина", "📡", listOf("Схема топологии", "Узлы BCM, BSM, ABS", "Диагностика шины")),
                    EncSystem("immo", "ЭСУД и иммобилайзер", "🔐", listOf("Синхронизация", "Обучение ключей")),
                    EncSystem("body", "Электросистемы кузова (BCM)", "🚗", listOf("Освещение", "Стеклоочистители")),
                    EncSystem("climate", "Климатическое оборудование", "❄️", listOf("Электросхема", "Коды климата")),
                    EncSystem("battery", "АКБ и управление зарядом", "🔋", listOf("Утечки", "Контроль генератора")),
                    EncSystem("dtc", "Коды ошибок по системам", "🛠️", listOf("DTC-каталог"))
                )),
                EncPlatform("vesta_akpp", "Веста / XRAY с АКПП", "📂", listOf(
                    EncSystem("tcu", "Электронное управление АКПП (TCU)", "⚙️", listOf("Распиновка", "Схема")),
                    EncSystem("dtc", "Коды ошибок трансмиссии", "❌", listOf("Расшифровка")),
                ))
            )
        )
        ) // close brands for russia
        ), // close EncCountry russia

        // Германия
        EncCountry(
            id = "germany",
            displayName = "Германия",
            flagEmoji = "🇩🇪",
            brands = listOf(
        EncBrand(
            id = "volkswagen", displayName = "Volkswagen", shortName = "VW",
            primaryColor = Color(0xFF003399), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_volkswagen,
            platforms = listOf(
                EncPlatform("vag_common", "Общая архитектура VAG", "📂", listOf(
                    EncSystem("can", "CAN-шина: топология", "🌐", listOf("Comfort CAN", "Drive CAN", "Media CAN")),
                    EncSystem("lin", "LIN-шина: устройства", "🔌", listOf("Протокол", "Проверка")),
                    EncSystem("flexray", "FlexRay", "📡", listOf("Новые модели")),
                    EncSystem("blocks", "Номера блоков управления", "🖥️", listOf("Адреса 01–7F")),
                    EncSystem("coding", "Кодирование блоков", "🔧", listOf("Long Coding Helper", "Адаптации")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("Принцип работы", "Адаптация ключей"))
                )),
                EncPlatform("pq35", "Платформа PQ35 (Golf 5/6, Passat B6/B7)", "📂", listOf(
                    EncSystem("wiring", "Архитектура электросети", "⚡", listOf("Предохранители", "Массы")),
                    EncSystem("ecu", "ЭБУ двигателя", "🖥️", listOf("Распиновка", "DTC-список (P/U-коды)", "Мессблоки (Measuring Blocks)")),
                    EncSystem("bcm", "BCM (блок комфорта J519 / J393)", "🧠", listOf("Кодирование", "Типовые неисправности")),
                    EncSystem("abs", "ABS / ESP (J104)", "🛡️", listOf("Адаптация", "Датчики")),
                    EncSystem("airbag", "Airbag (J234)", "💺", listOf("Crash data", "Замена пиропатронов")),
                    EncSystem("climate", "Климат", "❄️", listOf("Climatronic vs Climatic", "Адаптация заслонок")),
                    EncSystem("battery", "Управление АКБ", "🔋", listOf("BST", "IBS-датчик"))
                )),
                EncPlatform("mqb", "Платформа MQB (Golf 7/8, Tiguan 2)", "📂", listOf(
                    EncSystem("mild_hybrid", "48V мягкий гибрид", "⚡", listOf("MQB evo")),
                    EncSystem("ecu", "ЭБУ (Bosch MG1, Continental SID310)", "🖥️", listOf("DTC", "Специфика")),
                    EncSystem("gateway", "Gateway (J533)", "📡", listOf("Схема подключения шин", "Диагностика через gateway")),
                    EncSystem("bcm", "BCM 2.0 (J519)", "🧠", listOf("Long coding", "Адаптации освещения")),
                    EncSystem("matrix", "Matrix LED (J431)", "💡", listOf("Калибровка", "Коды ошибок")),
                    EncSystem("cameras", "Камеры и ассистенты", "📷", listOf("Front Camera", "ACC")),
                    EncSystem("mib", "MIB2/MIB3", "🔐", listOf("Медиасистема", "Кодирование"))
                )),
                EncPlatform("diag", "VAG-диагностика (VCDS / OBD)", "📂", listOf(
                    EncSystem("addresses", "Адреса блоков и их функции", "🔧", listOf("Список адресов")),
                    EncSystem("measuring", "Мессблоки (Live Data)", "📊", listOf("Список полей", "Нормальные значения")),
                    EncSystem("adaptations", "Адаптационные каналы", "⚙️", listOf("Частые настройки")),
                    EncSystem("basic", "Базовые установки", "🔄", listOf("Дроссель", "EGR")),
                    EncSystem("dtc", "Расшифровка кодов ошибок", "❌", listOf("P/C/B/U коды"))
                ))
            )
        ),

        // Toyota
        EncBrand(
            id = "toyota", displayName = "Toyota", shortName = "Toyota",
            primaryColor = Color(0xFFEB0A1E), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_toyota,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Toyota", "📂", listOf(
                    EncSystem("can", "CAN-шина: Multi-CAN", "🌐", listOf("SFI", "Body", "DLC")),
                    EncSystem("proto", "Диагностический протокол", "📡", listOf("ISO 15765", "KWP2000")),
                    EncSystem("blocks", "Блоки управления и их ID", "🖥️", listOf("Список ECU")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("Типы (IMMO, Smart Entry)", "Адаптация ключей"))
                )),
                EncPlatform("ice", "Традиционные ДВС", "📂", listOf(
                    EncSystem("wiring", "Бортовая сеть 12V / 14V", "⚡", listOf("Реле", "Предохранители")),
                    EncSystem("ecu", "ЭБУ двигателя", "🖥️", listOf("Коды ошибок (Toyota DTC)", "Live Data", "Распиновки")),
                    EncSystem("sensors", "Датчики", "🌡️", listOf("Осциллограммы", "Коды отказов датчиков")),
                    EncSystem("abs", "ABS / VSC / TRAC", "🛡️", listOf("Адаптация", "Zero Point Calibration")),
                    EncSystem("airbag", "SRS Airbag", "💺", listOf("Считывание кодов скрепкой")),
                    EncSystem("smart_key", "Smart Key System", "🔑", listOf("Схема работы", "Мёртвые зоны антенны", "Диагностика")),
                    EncSystem("battery", "Управление АКБ", "🔋", listOf("Датчики тока"))
                )),
                EncPlatform("hybrid", "Hybrid Synergy Drive", "📂", listOf(
                    EncSystem("safety", "ВЫСОКОВОЛЬТНАЯ СИСТЕМА", "⚠️", listOf("Меры безопасности", "Отключение")),
                    EncSystem("hv_battery", "Высоковольтная АКБ", "🔋", listOf("Модули и ячейки", "Система охлаждения", "Блок управления (BMU)", "Коды ошибок (P0A0x)", "Балансировка")),
                    EncSystem("inverter", "Инвертор / Конвертер", "⚡", listOf("Принцип работы", "Коды ошибок инвертора", "Диагностика изоляции")),
                    EncSystem("charge", "Зарядная система", "🔌", listOf("12V / HV", "Проверка")),
                    EncSystem("hv_ecu", "HV ECU", "🖥️", listOf("Коды ошибок HV", "Мессблоки (SOC, температуры)", "Сервисный режим (SERVICE MODE)")),
                    EncSystem("safety_sys", "Система безопасности HV", "🛡️", listOf("SMR-реле", "Interlock"))
                )),
                EncPlatform("techstream", "Диагностика Toyota", "📂", listOf(
                    EncSystem("setup", "Подключение и настройка", "🔧", listOf("Techstream", "G-scan")),
                    EncSystem("live", "Параметры Live Data", "📊", listOf("Эталонные значения")),
                    EncSystem("active", "Активные тесты", "⚙️", listOf("Actuator Tests")),
                    EncSystem("dtc", "DTC каталог", "❌", listOf("General", "HV-специфика"))
                ))
            )
        ),

        // Ford
        EncBrand(
            id = "ford", displayName = "Ford", shortName = "Ford",
            primaryColor = Color(0xFF003478), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_ford,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Ford", "📂", listOf(
                    EncSystem("can", "CAN HS / MS / LS шины", "🌐", listOf("3-шинная топология")),
                    EncSystem("obd", "OBD-II протоколы", "📡", listOf("CAN ISO 15765")),
                    EncSystem("modules", "Модульные блоки управления", "🖥️", listOf("PCM", "BCM", "GEM", "PATS")),
                    EncSystem("pats", "PATS (Иммобилайзер)", "🔐", listOf("Поколения PATS", "Программирование ключей"))
                )),
                EncPlatform("c1", "Платформа C1 / C2 (Focus 2, C-Max)", "📂", listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Схемы GEM", "Болячки приборки")),
                    EncSystem("pcm", "PCM", "🖥️", listOf("Коды ошибок OBD-II", "Параметры PID (Live Data)")),
                    EncSystem("gem", "GEM", "🧠", listOf("Освещение, стеклоподъёмники", "Программирование конфигурации")),
                    EncSystem("abs", "ABS / RSC", "🛡️", listOf("Roll Stability Control")),
                    EncSystem("climate", "Климат-контроль", "❄️", listOf("Manual", "Auto", "Самодиагностика"))
                )),
                EncPlatform("c2_fwd", "Платформа C2 / FWD (Focus 3, Mondeo 5)", "📂", listOf(
                    EncSystem("arch", "Архитектура сети", "⚡", listOf("HS-CAN", "MS-CAN", "LIN")),
                    EncSystem("pcm", "PCM (EcoBoost)", "🖥️", listOf("Специфика EcoBoost", "Типовые коды (P0299)")),
                    EncSystem("bcm", "BCM", "🧠", listOf("Программирование параметров", "Блокировки")),
                    EncSystem("sync", "SYNC / MyFord Touch", "💡", listOf("Медиасистема", "Обновления")),
                    EncSystem("cam", "Камеры", "📷", listOf("Задняя", "360°")),
                    EncSystem("bms", "Управление АКБ (BMS)", "🔋", listOf("Сброс BMS"))
                )),
                EncPlatform("f_series", "F-Series / Ranger / Transit", "📂", listOf(
                    EncSystem("batt", "Dual Battery System", "⚡", listOf("Transit специфика")),
                    EncSystem("pcm", "PCM V8 / V6", "🖥️", listOf("EcoBoost V6", "PowerStroke")),
                    EncSystem("trans", "TorqShift АКПП", "⚙️", listOf("Электрическое управление")),
                    EncSystem("trailer", "Электрика прицепа", "🔌", listOf("7-pin Ford", "Модуль прицепа")),
                    EncSystem("safety", "Системы безопасности коммерческих", "🛡️", listOf("Специфика"))
                )),
                EncPlatform("ev", "Ford EV / Hybrid", "📂", listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("Mach-E", "F-150 Lightning")),
                    EncSystem("battery", "Traction Battery", "🔋", listOf("LFP / NMC ячейки")),
                    EncSystem("charge", "Зарядные системы", "⚡", listOf("AC / DC", "CCS")),
                    EncSystem("becm", "BECM", "🖥️", listOf("Battery Energy Control Module"))
                ))
            )
        ),

        // Audi
        EncBrand(
            id = "audi", displayName = "Audi", shortName = "Audi",
            primaryColor = Color(0xFFBB0A30), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_audi,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Audi", "📂", listOf(
                    EncSystem("can", "CAN / MOST шины", "🌐", listOf("Drive", "Comfort", "Media", "MOST оптика")),
                    EncSystem("gateway", "Центральный шлюз (J533)", "🖥️", listOf("Диагностика оптики")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("IMMO 4", "IMMO 5")),
                    EncSystem("key", "Advanced Key", "🔑", listOf("MMI-интеграция"))
                )),
                EncPlatform("mlb", "Платформа MLB / MLB Evo", "📂", listOf(
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
                EncPlatform("mmi", "MMI / Виртуальный кокпит", "📂", listOf(
                    EncSystem("mmi_gen", "MMI 2G / 3G / 3G+", "🖥️", listOf("Кодирование", "Скрытые меню")),
                    EncSystem("vc", "Virtual Cockpit", "📊", listOf("MIB2", "MIB3")),
                    EncSystem("carplay", "CarPlay / Android Auto", "📱", listOf("Активация"))
                )),
                EncPlatform("odis", "ODIS / VCDS специфика", "📂", listOf(
                    EncSystem("guided", "Guided Functions", "🔧", listOf("Сервисные процедуры")),
                    EncSystem("long", "Long Coding Helper", "⚙️", listOf("Audi специфика")),
                    EncSystem("labels", "Label-файлы", "📊", listOf("Мессблоки"))
                ))
            )
        ),

        // Chevrolet
        EncBrand(
            id = "chevrolet", displayName = "Chevrolet", shortName = "Chevy",
            primaryColor = Color(0xFFD4AF37), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_chevrolet,
            platforms = listOf(
                EncPlatform("gm_common", "Общая архитектура GM", "📂", listOf(
                    EncSystem("gmlan", "GMLAN HS / LS", "🌐", listOf("GM CAN")),
                    EncSystem("proto", "Протоколы", "📡", listOf("Class 2", "GMLAN")),
                    EncSystem("blocks", "GM-блоки", "🖥️", listOf("ECM", "BCM", "TCM", "EBCM", "SCM")),
                    EncSystem("passlock", "PassLock / VATS", "🔐", listOf("GM Passlock 3", "Процедура сброса"))
                )),
                EncPlatform("delta", "Платформа Delta (Cruze)", "📂", listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("Блоки предохранителей")),
                    EncSystem("ecm", "ECM", "🖥️", listOf("Коды ошибок GM DTC", "PID Data List", "Сервисные процедуры")),
                    EncSystem("bcm", "BCM", "🧠", listOf("Программирование", "Управляемые функции")),
                    EncSystem("ebcm", "EBCM", "🛡️", listOf("Тормозная система")),
                    EncSystem("climate", "Климат", "❄️", listOf("HVAC модуль")),
                    EncSystem("rke", "Remote Keyless Entry", "🔑", listOf("RKE"))
                )),
                EncPlatform("gamma", "Платформа Gamma (Spark, Sonic)", "📂", listOf(
                    EncSystem("wiring", "Упрощённая архитектура", "⚡", listOf("Особенности")),
                    EncSystem("ecm", "ECM", "🖥️", listOf("Малообъёмные двигатели")),
                    EncSystem("trouble", "Типовые неисправности", "🛠️", listOf("Электрика"))
                )),
                EncPlatform("gmt", "GMT (Silverado, Tahoe)", "📂", listOf(
                    EncSystem("wiring", "Двойная АКБ / Генератор", "⚡", listOf("Heavy duty")),
                    EncSystem("ecm", "ECM V8", "🖥️", listOf("AFM / DFM", "Диагностика масляной системы AFM")),
                    EncSystem("trans", "Hydra-Matic АКПП", "⚙️", listOf("Электрическое управление", "Коды ошибок")),
                    EncSystem("trailer", "Электрика прицепа", "🔌", listOf("7-pin GM")),
                    EncSystem("drl", "Daytime Running Lights", "💡", listOf("GM DRL"))
                )),
                EncPlatform("ev", "EV / Hybrid (Bolt, Volt)", "📂", listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("Основы")),
                    EncSystem("batt", "Ultium Battery", "🔋", listOf("Thermal Management System")),
                    EncSystem("charge", "Зарядка", "⚡", listOf("J1772", "CCS Combo")),
                    EncSystem("pim", "BECM / PIM", "🖥️", listOf("Power Inverter Module"))
                ))
            )
        ),

        // Mitsubishi
        EncBrand(
            id = "mitsubishi", displayName = "Mitsubishi", shortName = "Mitsu",
            primaryColor = Color(0xFFED1A3B), secondaryColor = Color(0xFF000000),
            logoResId = R.drawable.img_logo_mitsubishi,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Mitsubishi", "📂", listOf(
                    EncSystem("can", "CAN-шина", "🌐", listOf("M-CAN", "Body CAN")),
                    EncSystem("mut", "Протокол MUT-II / MUT-III", "📡", listOf("Фирменный сканер")),
                    EncSystem("blocks", "Блоки", "🖥️", listOf("ECM", "TCM", "ABS", "SRS", "BCM", "EPS")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("MIT-Immo", "Регистрация ключей"))
                )),
                EncPlatform("outlander", "Outlander / ASX", "📂", listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("ETACS")),
                    EncSystem("ecm", "ECM (MPI / GDI)", "🖥️", listOf("Коды ошибок", "Live Data")),
                    EncSystem("awc", "AWD / S-AWC", "🚗", listOf("Электромуфта", "Диагностика")),
                    EncSystem("hvac", "HVAC", "❄️", listOf("Manual", "Digital")),
                    EncSystem("kessy", "Keyless Entry", "🔑", listOf("KESSY"))
                )),
                EncPlatform("lancer", "Lancer / Evolution", "📂", listOf(
                    EncSystem("evo", "Специфика Evo", "⚡", listOf("Жгуты проводки")),
                    EncSystem("ecm", "ECM (4B11 Evo X)", "🖥️", listOf("Наддув", "Коды")),
                    EncSystem("acd", "ACD / AYC", "🚗", listOf("Гидравлический ACD", "Коды ошибок", "Процедура обслуживания")),
                    EncSystem("abs", "ABS с интеграцией AYC", "🛡️", listOf("Особенности"))
                )),
                EncPlatform("l200", "L200 / Pajero", "📂", listOf(
                    EncSystem("wiring", "24V система (дизель)", "⚡", listOf("Pajero")),
                    EncSystem("ecm", "ECM дизельных", "🖥️", listOf("CRDI", "Диагностика давления Rail")),
                    EncSystem("trailer", "Электрика прицепа", "🔌", listOf("Распиновка")),
                    EncSystem("4wd", "Super Select 4WD II", "🚗", listOf("Электронное управление"))
                )),
                EncPlatform("phev", "Outlander PHEV", "📂", listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("300V+")),
                    EncSystem("battery", "HV-аккумулятор", "🔋", listOf("Li-ion 12 kWh")),
                    EncSystem("motors", "Электромоторы", "⚡", listOf("Front / Rear MG")),
                    EncSystem("charge", "Зарядная система", "🔌", listOf("CHAdeMO", "Type 1")),
                    EncSystem("ev_ecu", "EV-ECU", "🖥️", listOf("Управление энергопотоками"))
                ))
            )
        ),

        // Honda
        EncBrand(
            id = "honda", displayName = "Honda", shortName = "Honda",
            primaryColor = Color(0xFFCC0000), secondaryColor = Color(0xFFFFFFFF),
            logoResId = R.drawable.img_logo_honda,
            platforms = listOf(
                EncPlatform("common", "Общая архитектура Honda", "📂", listOf(
                    EncSystem("can", "CAN шина (F-CAN, B-CAN)", "🌐", listOf("Топология F-CAN", "Топология B-CAN")),
                    EncSystem("hds", "Протокол Honda HDS", "📡", listOf("ISO 15765-4")),
                    EncSystem("blocks", "Блоки", "🖥️", listOf("ECM/PCM", "TCM", "ABS/VSA", "SRS", "BCM", "EPS")),
                    EncSystem("immo", "Иммобилайзер", "🔐", listOf("Тип 1 / Тип 2", "Регистрация ключей"))
                )),
                EncPlatform("civic", "Civic / Accord / CR-V", "📂", listOf(
                    EncSystem("wiring", "Бортовая сеть", "⚡", listOf("MICU")),
                    EncSystem("pcm", "ECM/PCM", "🖥️", listOf("Коды ошибок Honda", "Флэш-программирование")),
                    EncSystem("vtec", "VTEC / i-VTEC", "🌡️", listOf("Соленоид VTEC", "OCV", "Коды ошибок (P2646)")),
                    EncSystem("cvt", "CVT вариатор", "⚙️", listOf("Электронное управление TCM", "Коды CVT (P17xx)", "Процедура замены ATF")),
                    EncSystem("vsa", "VSA", "🛡️", listOf("Vehicle Stability Assist")),
                    EncSystem("airbag", "SRS Airbag", "💺", listOf("Honda-специфика")),
                    EncSystem("climate", "Климат", "❄️", listOf("Dual Zone"))
                )),
                EncPlatform("fit", "Fit / Jazz", "📂", listOf(
                    EncSystem("wiring", "Упрощённая архитектура", "⚡", listOf("Особенности")),
                    EncSystem("ecm", "ECM", "🖥️", listOf("PGM-FI управление")),
                    EncSystem("4wd", "Real Time AWD", "🚗", listOf("Электромуфта")),
                    EncSystem("trouble", "Типовые проблемы", "🛠️", listOf("Электрика"))
                )),
                EncPlatform("hybrid", "IMA / e:HEV Hybrid", "📂", listOf(
                    EncSystem("hv", "Высоковольтная система", "⚠️", listOf("100–200V IMA", "250V+ e:HEV")),
                    EncSystem("batt", "IMA Battery", "🔋", listOf("Модули и балансировка", "Коды ошибок (P13xx)")),
                    EncSystem("pcu", "IMA Motor / PCU", "⚡", listOf("Принцип", "Диагностика PCU")),
                    EncSystem("ehev", "e:HEV", "🔌", listOf("Отличие от IMA", "Специфические коды"))
                )),
                EncPlatform("diag", "Honda Diagnostics", "📂", listOf(
                    EncSystem("hds", "Honda HDS", "🔧", listOf("Настройка")),
                    EncSystem("live", "Live Data", "📊", listOf("Параметры по блокам")),
                    EncSystem("adapt", "Сервисные процедуры", "⚙️", listOf("Обучение дроссельной заслонки", "Адаптация руля EPS", "Сброс Maintenance Minder")),
                    EncSystem("dtc", "Honda DTC каталог", "❌", listOf("VTEC / CVT специфика"))
                ))
            )
        )
        ) // close brands for germany
        ) // close EncCountry germany
    ) // close countries list

    // Flat list of all brands for backward compatibility and cross-searching
    val brands: List<EncBrand>
        get() = countries.flatMap { it.brands }
}
