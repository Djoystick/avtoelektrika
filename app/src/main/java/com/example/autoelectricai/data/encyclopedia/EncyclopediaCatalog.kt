package com.example.autoelectricai.data.encyclopedia

import androidx.compose.ui.graphics.Color

/**
 * Static encyclopedia catalog structure.
 * Each brand has platforms (model families/eras), each platform has systems, each system has subsystems.
 * This structure matches the AI categorization fields: encyclopediaPlatform / encyclopediaSystem / encyclopediaSubsystem.
 */

data class EncBrand(
    val id: String,
    val displayName: String,
    val shortName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val logoEmoji: String,
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

    private val commonSystems = listOf(
        EncSystem("ecu", "ЭБУ двигателя", "🖥️", listOf("Распиновка", "Коды ошибок OBD-II", "Адаптация и настройка", "Замена ЭБУ")),
        EncSystem("sensors", "Датчики", "🌡️", listOf("ДМРВ / MAP", "Кислородный датчик", "ДТОЖ / ДПДЗ", "Датчик давления", "Датчик коленвала")),
        EncSystem("power_net", "Бортовая сеть", "⚡", listOf("Генератор", "АКБ и зарядка", "Предохранители и реле", "Масса кузова")),
        EncSystem("lighting", "Освещение", "💡", listOf("Фары", "Задние фонари", "DRL / Противотуманки", "Внутреннее освещение")),
        EncSystem("abs_esp", "ABS / Стабилизация", "🛡️", listOf("Датчики скорости колёс", "Гидроблок ABS", "Коды ошибок ABS/ESP")),
        EncSystem("airbag", "Airbag / SRS", "💺", listOf("Блок управления SRS", "Датчики удара", "Ремни безопасности", "Коды ошибок SRS")),
        EncSystem("climate", "Климат", "❄️", listOf("Компрессор кондиционера", "Вентилятор печки", "Блок управления климатом", "Коды ошибок климата")),
        EncSystem("immobilizer", "Иммобилайзер", "🔐", listOf("Принцип работы", "Регистрация ключей", "Обход и сброс")),
        EncSystem("can_bus", "CAN / LIN шина", "📡", listOf("Топология сети", "Диагностика шины", "Осциллограммы")),
        EncSystem("gearbox", "Трансмиссия", "⚙️", listOf("АКПП — блок управления TCM", "Коды ошибок АКПП", "Адаптация и сброс TCM"))
    )

    val brands: List<EncBrand> = listOf(

        EncBrand(
            id = "vaz", displayName = "ВАЗ (LADA)", shortName = "ВАЗ",
            primaryColor = Color(0xFFCC0000), secondaryColor = Color(0xFF1A1A2E),
            logoEmoji = "🇷🇺",
            platforms = listOf(
                EncPlatform("classic", "Классика (2101–2107)", "📂", listOf(
                    EncSystem("ignition", "Зажигание", "🔋", listOf("Контактное зажигание", "Бесконтактное зажигание", "Катушка и распределитель")),
                    EncSystem("power_net", "Бортовая сеть 12V", "⚡", listOf("Генератор", "Реле-регулятор", "Предохранители")),
                    EncSystem("lighting", "Освещение", "💡", listOf("Фары", "Задние фонари", "Световая сигнализация"))
                )),
                EncPlatform("samara", "Самара / Спутник (2108–2115)", "📂", listOf(
                    EncSystem("ecu_jan", "ЭБУ Январь / Bosch", "🖥️", listOf("Распиновка Январь 4/5", "Коды ошибок", "Диагностика")),
                    EncSystem("carb_inj", "Карбюратор → Инжектор", "🔋", listOf("Карбюратор ДААЗ", "Переход на инжектор", "Типовые проблемы")),
                    EncSystem("power_net", "Бортовая сеть", "⚡", listOf("Генератор", "Предохранители", "Проводка кузова"))
                )),
                EncPlatform("priora_kalina", "Десятка / Калина / Приора (2110–2172)", "📂",
                    commonSystems + listOf(
                        EncSystem("ecu_specific", "ЭБУ Bosch M7.9.7 / VS 8.0", "🖥️", listOf("Распиновка", "Коды ошибок OBD-II", "Адаптация"))
                    )
                ),
                EncPlatform("granta_vesta", "Гранта / Веста / XRAY / Нива Travel", "📂",
                    commonSystems + listOf(
                        EncSystem("ecu_me17", "ЭБУ Bosch ME17 / Continental SID", "🖥️", listOf("Протоколы ISO 15765 / KWP2000", "Адаптации", "Прошивки")),
                        EncSystem("can_lin", "CAN/LIN мультиплекс", "📡", listOf("Топология", "BCM / BSM", "Диагностика шины")),
                        EncSystem("akpp", "АКПП (Веста/XRAY)", "⚙️", listOf("TCU управление", "Коды ошибок трансмиссии", "Сброс адаптации"))
                    )
                )
            )
        ),

        EncBrand(
            id = "volkswagen", displayName = "Volkswagen", shortName = "VW",
            primaryColor = Color(0xFF003399), secondaryColor = Color(0xFFFFFFFF),
            logoEmoji = "🇩🇪",
            platforms = listOf(
                EncPlatform("vag_common", "Общая архитектура VAG", "📂", listOf(
                    EncSystem("vcds_diag", "VCDS Диагностика", "🔧", listOf("Адреса блоков 01–7F", "Мессблоки (Live Data)", "Адаптации", "Базовые установки")),
                    EncSystem("coding", "Кодирование блоков", "⚙️", listOf("Long Coding Helper", "Byte-кодирование", "Coding примеры")),
                    EncSystem("immo", "Иммобилайзер IMMO 3/4/5", "🔐", listOf("Принцип работы", "Адаптация ключей"))
                )),
                EncPlatform("pq35", "Платформа PQ35 (Golf 5/6, Passat B6/B7)", "📂",
                    commonSystems + listOf(
                        EncSystem("bcm_j519", "BCM J519 / J393", "🧠", listOf("Кодирование", "Типовые неисправности")),
                        EncSystem("akb_mgmt", "Управление АКБ (IBS)", "🔋", listOf("BST датчик", "AGM / EFB АКБ"))
                    )
                ),
                EncPlatform("mqb", "Платформа MQB (Golf 7/8, Polo 6, Tiguan 2)", "📂",
                    commonSystems + listOf(
                        EncSystem("gateway_j533", "Gateway J533", "📡", listOf("Топология шин", "Диагностика через шлюз")),
                        EncSystem("matrix_led", "Matrix LED (J431)", "💡", listOf("Кодирование Matrix", "Диагностика ассистентов")),
                        EncSystem("mib", "MIB2/MIB3 Медиасистема", "📺", listOf("Активация CarPlay/AA", "Кодирование"))
                    )
                )
            )
        ),

        EncBrand(
            id = "toyota", displayName = "Toyota", shortName = "Toyota",
            primaryColor = Color(0xFFEB0A1E), secondaryColor = Color(0xFF1E1E1E),
            logoEmoji = "🇯🇵",
            platforms = listOf(
                EncPlatform("toyota_common", "Общая архитектура", "📂", listOf(
                    EncSystem("techstream", "Диагностика Techstream", "🔧", listOf("Live Data", "Активные тесты", "DTC каталог")),
                    EncSystem("smart_key", "Smart Key System", "🔑", listOf("Схема работы", "Мёртвые зоны антенны", "Диагностика"))
                )),
                EncPlatform("trad_ice", "ДВС (Corolla, Camry, RAV4, LC)", "📂",
                    commonSystems + listOf(
                        EncSystem("sensors_toyota", "Датчики Toyota", "🌡️", listOf("MAF / MAP", "O2 датчик", "Knock датчик", "Осциллограммы"))
                    )
                ),
                EncPlatform("hsd", "🔋 Hybrid Synergy Drive (Prius, RAV4h)", "📂", listOf(
                    EncSystem("hv_safety", "⚠️ Высоковольтная безопасность", "⚠️", listOf("Меры защиты (200–650V)", "Отключение HV системы", "SMR-реле Interlock")),
                    EncSystem("hv_battery", "HV Аккумулятор", "🔋", listOf("Модули NiMH / Li-ion", "Охлаждение АКБ", "BMU управление", "Коды P0A0x", "Балансировка ячеек")),
                    EncSystem("inverter", "Инвертор / Конвертер MG1/MG2", "⚡", listOf("Принцип работы", "Коды ошибок", "Диагностика изоляции")),
                    EncSystem("hv_ecu", "HV ECU", "🖥️", listOf("Коды ошибок HV", "SOC / температуры / токи", "Сервисный режим"))
                ))
            )
        ),

        EncBrand(
            id = "ford", displayName = "Ford", shortName = "Ford",
            primaryColor = Color(0xFF003DA5), secondaryColor = Color(0xFFFFFFFF),
            logoEmoji = "🇺🇸",
            platforms = listOf(
                EncPlatform("ford_common", "Общая архитектура Ford", "📂", listOf(
                    EncSystem("can_topology", "CAN HS/MS/LS топология", "🌐", listOf("3-шинная архитектура", "Устройства на каждой шине")),
                    EncSystem("pats", "PATS Иммобилайзер", "🔐", listOf("Поколения PATS 1/2/3", "Программирование ключей"))
                )),
                EncPlatform("c1_c2", "Платформа C1/C2 (Focus 2, C-Max, Kuga 1)", "📂",
                    commonSystems + listOf(
                        EncSystem("gem", "GEM — Generic Electronic Module", "🧠", listOf("Функции GEM", "Программирование конфигурации"))
                    )
                ),
                EncPlatform("c2_fwd", "Платформа C2 (Focus 3, EcoSport, Mondeo 5)", "📂",
                    commonSystems + listOf(
                        EncSystem("ecoboost", "EcoBoost (1.0T / GTDI)", "🖥️", listOf("Специфика наддува", "Типовые коды P0299", "Клапан VCT")),
                        EncSystem("sync", "SYNC / MyFord Touch", "📺", listOf("Кодирование", "Обновление прошивки"))
                    )
                ),
                EncPlatform("commercial", "F-Series / Ranger / Transit", "📂",
                    commonSystems + listOf(
                        EncSystem("dual_battery", "Dual Battery System (Transit)", "🔋", listOf("Схема двух АКБ", "Управление зарядом")),
                        EncSystem("trailer", "Электрика прицепа (7-pin)", "🔌", listOf("Распиновка 7-pin", "Типовые проблемы"))
                    )
                )
            )
        ),

        EncBrand(
            id = "audi", displayName = "Audi", shortName = "Audi",
            primaryColor = Color(0xFF1A1A1A), secondaryColor = Color(0xFFC0C0C0),
            logoEmoji = "🇩🇪",
            platforms = listOf(
                EncPlatform("audi_common", "Общая архитектура Audi", "📂", listOf(
                    EncSystem("odis_diag", "ODIS / VCDS Диагностика", "🔧", listOf("Guided Functions", "Long Coding Audi", "Label-файлы мессблоков")),
                    EncSystem("most_bus", "MOST-шина (оптоволокно)", "📡", listOf("Медиа и навигация", "Устройства на шине")),
                    EncSystem("gateway_a", "Центральный шлюз J533", "🌐", listOf("Топология шин", "FlexRay"))
                )),
                EncPlatform("mlb", "Платформа MLB (A4 B8/B9, A6 C7/C8, Q5, Q7)", "📂",
                    commonSystems + listOf(
                        EncSystem("flexray", "FlexRay шина (A6 C7+)", "📡", listOf("Топология", "Диагностика ODIS Engineering")),
                        EncSystem("air_susp", "Пневмоподвеска Air Suspension", "💺", listOf("Компрессор J403", "Клапанный блок", "Коды ошибок пневмы", "Диагностика давлений")),
                        EncSystem("matrix_audi", "Matrix LED / OLED", "💡", listOf("Блок J431", "Кодирование Matrix", "Диагностика ассистентов")),
                        EncSystem("quattro", "quattro (Haldex 5 / Torsen C)", "🚗", listOf("Электромуфта Haldex", "Коды ошибок", "Замена масла Haldex")),
                        EncSystem("mmi", "MMI / Virtual Cockpit", "📺", listOf("MMI 2G/3G/3G+", "Virtual Cockpit MIB2/MIB3", "CarPlay / AA активация"))
                    )
                )
            )
        ),

        EncBrand(
            id = "chevrolet", displayName = "Chevrolet", shortName = "Chevy",
            primaryColor = Color(0xFFD4AF37), secondaryColor = Color(0xFF1A1A1A),
            logoEmoji = "🇺🇸",
            platforms = listOf(
                EncPlatform("gm_common", "Общая архитектура GM", "📂", listOf(
                    EncSystem("gmlan", "GMLAN HS/LS шина", "🌐", listOf("Протоколы Class 2 / GMLAN", "Устройства на шинах")),
                    EncSystem("passlock", "PassLock / VATS", "🔐", listOf("Поколения системы", "Сброс и обучение"))
                )),
                EncPlatform("delta", "Платформа Delta (Cruze, Astra J, Zafira C)", "📂",
                    commonSystems + listOf(
                        EncSystem("ecm_gm", "ECM Bosch ME17.9 / Delco E78", "🖥️", listOf("GM DTC коды P/B/C/U", "PID Data List", "Адаптация дросселя")),
                        EncSystem("rke", "Remote Keyless Entry (RKE)", "🔑", listOf("Программирование брелока", "Диагностика"))
                    )
                ),
                EncPlatform("gmt", "GMT (Silverado, Tahoe, Suburban)", "📂",
                    commonSystems + listOf(
                        EncSystem("afm", "AFM / DFM (отключение цилиндров)", "🖥️", listOf("Принцип AFM", "Диагностика масляной системы AFM", "Коды ошибок")),
                        EncSystem("hydramatic", "Hydra-Matic 8L90/10L90 АКПП", "⚙️", listOf("TCM управление", "Коды ошибок трансмиссии"))
                    )
                )
            )
        ),

        EncBrand(
            id = "mitsubishi", displayName = "Mitsubishi", shortName = "Mitsubishi",
            primaryColor = Color(0xFFCC0000), secondaryColor = Color(0xFF1A1A1A),
            logoEmoji = "🇯🇵",
            platforms = listOf(
                EncPlatform("mit_common", "Общая архитектура Mitsubishi", "📂", listOf(
                    EncSystem("mut3", "Диагностика MUT-III", "🔧", listOf("Подключение MUT-III", "Live Data", "Активные тесты", "DTC каталог")),
                    EncSystem("mit_immo", "Иммобилайзер MIT-Immo", "🔐", listOf("Типы 2/3", "Регистрация ключей MUT-III"))
                )),
                EncPlatform("outlander", "Outlander / ASX / Eclipse Cross", "📂",
                    commonSystems + listOf(
                        EncSystem("sawc", "AWD / S-AWC", "🚗", listOf("Электромуфта TCM AWD", "Диагностика", "Адаптация"))
                    )
                ),
                EncPlatform("lancer_evo", "Lancer / Lancer Evolution", "📂",
                    commonSystems + listOf(
                        EncSystem("acd_ayc", "ACD / AYC Активный дифференциал", "🚗", listOf("Принцип работы", "Гидравлика ACD", "Коды ошибок", "Обслуживание"))
                    )
                ),
                EncPlatform("l200_pajero", "L200 / Pajero / Montero", "📂",
                    commonSystems + listOf(
                        EncSystem("crdi", "CRDI Common Rail (4M41, 4D56)", "🖥️", listOf("Диагностика давления Rail", "Форсунки", "Коды ошибок дизель")),
                        EncSystem("ss4wd", "Super Select 4WD II", "🚗", listOf("Электронное управление", "Коды ошибок"))
                    )
                ),
                EncPlatform("phev", "Outlander PHEV", "📂", listOf(
                    EncSystem("hv_safety_mit", "⚠️ Высоковольтная система (300V+)", "⚠️", listOf("Меры безопасности", "SMR реле")),
                    EncSystem("hv_bat_mit", "HV Аккумулятор Li-ion 12 kWh", "🔋", listOf("Ячейки", "Охлаждение", "Коды ошибок")),
                    EncSystem("ev_ecu", "EV-ECU", "🖥️", listOf("Управление энергопотоками", "Два MG мотора")),
                    EncSystem("charging_mit", "Зарядная система CHAdeMO", "🔌", listOf("CHAdeMO / Type 1", "Коды ошибок зарядки"))
                ))
            )
        ),

        EncBrand(
            id = "honda", displayName = "Honda", shortName = "Honda",
            primaryColor = Color(0xFFCC0000), secondaryColor = Color(0xFF1A1A1A),
            logoEmoji = "🇯🇵",
            platforms = listOf(
                EncPlatform("honda_common", "Общая архитектура Honda", "📂", listOf(
                    EncSystem("hds_diag", "Диагностика HDS / i-HDS", "🔧", listOf("Live Data по блокам", "Активные тесты", "Honda DTC каталог")),
                    EncSystem("honda_immo", "Иммобилайзер H-Immo", "🔐", listOf("Тип 1 / Тип 2", "Регистрация ключей")),
                    EncSystem("f_b_can", "F-CAN / B-CAN шина", "📡", listOf("Топология F-CAN", "Топология B-CAN"))
                )),
                EncPlatform("civic_accord", "Civic / Accord / CR-V (2012–2022)", "📂",
                    commonSystems + listOf(
                        EncSystem("vtec", "VTEC / i-VTEC", "🌡️", listOf("Соленоид VTEC", "OCV клапан фаз", "Коды P2646/P2647")),
                        EncSystem("cvt", "CVT вариатор Earth Dreams", "⚙️", listOf("TCM управление", "Коды CVT P17xx", "Замена ATF + адаптация"))
                    )
                ),
                EncPlatform("fit_jazz", "Fit / Jazz / HR-V", "📂",
                    commonSystems + listOf(
                        EncSystem("awd_honda", "Real Time AWD (электромуфта)", "🚗", listOf("Схема муфты", "Диагностика", "Типовые проблемы"))
                    )
                ),
                EncPlatform("hybrid_honda", "🔋 IMA / e:HEV Hybrid", "📂", listOf(
                    EncSystem("hv_safety_honda", "⚠️ Высоковольтная система", "⚠️", listOf("IMA: 100–200V", "e:HEV: 250V+", "Меры безопасности")),
                    EncSystem("ima_battery", "IMA Battery (NiMH / Li-ion)", "🔋", listOf("Модули", "Балансировка", "Коды P13xx")),
                    EncSystem("pcu", "PCU — Power Control Unit", "⚡", listOf("Принцип IMA мотора", "e:HEV два мотора", "Диагностика PCU")),
                    EncSystem("maintenance_minder", "Сервисные процедуры Honda", "🔧", listOf("Обучение дросселя", "Адаптация EPS", "Сброс Maintenance Minder"))
                ))
            )
        )
    )

    /** Returns brand by id */
    fun getBrandById(id: String) = brands.find { it.id == id }

    /** Returns flat list of all platform names for AI prompt */
    fun getAllPlatformNames(): String = brands.flatMap { b ->
        b.platforms.map { "${b.displayName}: ${it.displayName}" }
    }.joinToString("\n")

    /** Returns flat list of all system names for AI prompt */
    fun getAllSystemNames(): String = brands.flatMap { b ->
        b.platforms.flatMap { p -> p.systems.map { "  ${it.displayName}" } }
    }.distinct().joinToString("\n")
}
