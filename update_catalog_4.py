import re

with open('app/src/main/java/com/example/autoelectricai/data/encyclopedia/EncyclopediaCatalog.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update EncBrand logoResId
content = content.replace('val logoResId: Int,', 'val logoResId: Int?,')

# 2. Update EncPlatform data class
content = content.replace(
    'data class EncPlatform(\n    val id: String,\n    val displayName: String,\n    val icon: String,\n    val systems: List<EncSystem>\n)',
    'data class EncPlatform(\n    val id: String,\n    val displayName: String,\n    val icon: String,\n    val startYear: Int,\n    val endYear: Int?,\n    val systems: List<EncSystem>\n)'
)

# 3. Update EncPlatform instances. 
content = re.sub(
    r'EncPlatform\("([^"]+)", "([^"]+)", "([^"]+)", listOf\(',
    r'EncPlatform("\1", "\2", "\3", 2000, 2025, listOf(',
    content
)

# 4. Add new countries to the countries list
countries_str = '''
        EncCountry("china", "Китай", "🇨🇳", brandsFlat.filter { it.id in listOf("chery", "haval", "geely", "changan", "exeed", "omoda", "byd", "lifan", "great_wall", "faw") }),
        EncCountry("korea", "Корея", "🇰🇷", brandsFlat.filter { it.id in listOf("hyundai", "kia", "genesis", "ssangyong") }),
        EncCountry("france", "Франция", "🇫🇷", brandsFlat.filter { it.id in listOf("renault", "peugeot", "citroen") }),
        EncCountry("uk", "Великобритания", "🇬🇧", brandsFlat.filter { it.id in listOf("land_rover", "jaguar", "bentley") }),
        EncCountry("italy", "Италия", "🇮🇹", brandsFlat.filter { it.id in listOf("fiat", "alfa_romeo") }),
        EncCountry("sweden", "Швеция", "🇸🇪", brandsFlat.filter { it.id in listOf("volvo") }),
        EncCountry("czech", "Чехия", "🇨🇿", brandsFlat.filter { it.id in listOf("skoda") }),
        EncCountry("spain", "Испания", "🇪🇸", brandsFlat.filter { it.id in listOf("seat") })
'''
# update existing countries
content = content.replace(
    'EncCountry("germany", "Германия", "🇩🇪", brandsFlat.filter { it.id in listOf("volkswagen", "audi") })',
    'EncCountry("germany", "Германия", "🇩🇪", brandsFlat.filter { it.id in listOf("volkswagen", "audi", "bmw", "mercedes", "porsche", "opel", "mini") })'
)
content = content.replace(
    'EncCountry("japan", "Япония", "🇯🇵", brandsFlat.filter { it.id in listOf("toyota", "mitsubishi", "honda") })',
    'EncCountry("japan", "Япония", "🇯🇵", brandsFlat.filter { it.id in listOf("toyota", "mitsubishi", "honda", "nissan", "mazda", "subaru", "suzuki", "lexus", "infiniti", "acura", "datsun") })'
)
content = content.replace(
    'EncCountry("russia", "Россия", "🇷🇺", brandsFlat.filter { it.id in listOf("vaz") })',
    'EncCountry("russia", "Россия", "🇷🇺", brandsFlat.filter { it.id in listOf("vaz", "gaz", "uaz") })'
)
content = content.replace(
    'EncCountry("usa", "США", "🇺🇸", brandsFlat.filter { it.id in listOf("ford", "chevrolet") })',
    'EncCountry("usa", "США", "🇺🇸", brandsFlat.filter { it.id in listOf("ford", "chevrolet", "jeep", "dodge", "chrysler", "cadillac", "tesla") }),' + countries_str
)

brands_to_add = [
    ("chery", "Chery", "Chery", "0xFF003366", "0xFFFFFFFF"),
    ("haval", "Haval", "Haval", "0xFFD0112B", "0xFFFFFFFF"),
    ("geely", "Geely", "Geely", "0xFF00438A", "0xFFFFFFFF"),
    ("changan", "Changan", "Changan", "0xFF005BAC", "0xFFFFFFFF"),
    ("exeed", "Exeed", "Exeed", "0xFF1A1A1A", "0xFFFFFFFF"),
    ("omoda", "Omoda", "Omoda", "0xFF000000", "0xFFFFFFFF"),
    ("byd", "BYD", "BYD", "0xFFDE0E19", "0xFFFFFFFF"),
    ("lifan", "Lifan", "Lifan", "0xFF0053A0", "0xFFFFFFFF"),
    ("great_wall", "Great Wall", "Great Wall", "0xFFE31B23", "0xFFFFFFFF"),
    ("faw", "FAW", "FAW", "0xFF00387B", "0xFFFFFFFF"),
    
    ("hyundai", "Hyundai", "Hyundai", "0xFF002C5F", "0xFFFFFFFF"),
    ("kia", "Kia", "Kia", "0xFF05141F", "0xFFFFFFFF"),
    ("genesis", "Genesis", "Genesis", "0xFF1A1A1A", "0xFFFFFFFF"),
    ("ssangyong", "SsangYong", "SsangYong", "0xFF004D95", "0xFFFFFFFF"),
    
    ("nissan", "Nissan", "Nissan", "0xFFC3002F", "0xFFFFFFFF"),
    ("mazda", "Mazda", "Mazda", "0xFF101010", "0xFFFFFFFF"),
    ("subaru", "Subaru", "Subaru", "0xFF00458C", "0xFFFFFFFF"),
    ("suzuki", "Suzuki", "Suzuki", "0xFFE31837", "0xFFFFFFFF"),
    ("lexus", "Lexus", "Lexus", "0xFF202020", "0xFFFFFFFF"),
    ("infiniti", "Infiniti", "Infiniti", "0xFF000000", "0xFFFFFFFF"),
    ("acura", "Acura", "Acura", "0xFF000000", "0xFFFFFFFF"),
    ("datsun", "Datsun", "Datsun", "0xFF004B87", "0xFFFFFFFF"),
    
    ("renault", "Renault", "Renault", "0xFFFFCC00", "0xFF000000"),
    ("peugeot", "Peugeot", "Peugeot", "0xFF002554", "0xFFFFFFFF"),
    ("citroen", "Citroen", "Citroen", "0xFFDA291C", "0xFFFFFFFF"),
    
    ("bmw", "BMW", "BMW", "0xFF0066B1", "0xFFFFFFFF"),
    ("mercedes", "Mercedes-Benz", "Mercedes", "0xFF1A1A1A", "0xFFFFFFFF"),
    ("porsche", "Porsche", "Porsche", "0xFFD4AF37", "0xFF000000"),
    ("opel", "Opel", "Opel", "0xFFF3E500", "0xFF000000"),
    ("mini", "Mini", "Mini", "0xFF000000", "0xFFFFFFFF"),
    
    ("jeep", "Jeep", "Jeep", "0xFF1A1A1A", "0xFFFFFFFF"),
    ("dodge", "Dodge", "Dodge", "0xFFE01825", "0xFFFFFFFF"),
    ("chrysler", "Chrysler", "Chrysler", "0xFF000000", "0xFFFFFFFF"),
    ("cadillac", "Cadillac", "Cadillac", "0xFF000000", "0xFFFFFFFF"),
    ("tesla", "Tesla", "Tesla", "0xFFCC0000", "0xFFFFFFFF"),
    
    ("uaz", "УАЗ (UAZ)", "УАЗ", "0xFF006633", "0xFFFFFFFF"),
    ("gaz", "ГАЗ", "ГАЗ", "0xFF1A1A1A", "0xFFFFFFFF"),
    
    ("land_rover", "Land Rover", "Land Rover", "0xFF005A2B", "0xFFFFFFFF"),
    ("jaguar", "Jaguar", "Jaguar", "0xFF000000", "0xFFFFFFFF"),
    ("bentley", "Bentley", "Bentley", "0xFF1A1A1A", "0xFFFFFFFF"),
    
    ("fiat", "Fiat", "Fiat", "0xFFE01825", "0xFFFFFFFF"),
    ("alfa_romeo", "Alfa Romeo", "Alfa Romeo", "0xFF9A0000", "0xFFFFFFFF"),
    
    ("volvo", "Volvo", "Volvo", "0xFF003057", "0xFFFFFFFF"),
    ("skoda", "Skoda", "Skoda", "0xFF4BA82E", "0xFFFFFFFF"),
    ("seat", "SEAT", "SEAT", "0xFFE31837", "0xFFFFFFFF")
]

kotlin_brands = []
for bid, dname, sname, pc, sc in brands_to_add:
    kotlin_brands.append(f'        EncBrand(id = "{bid}", displayName = "{dname}", shortName = "{sname}", primaryColor = Color({pc}), secondaryColor = Color({sc}), logoResId = null, platforms = emptyList()),')

kotlin_brands_str = "\n".join(kotlin_brands)

content = content.replace(
    '    )\n\n    val countries: List<EncCountry> = listOf(',
    kotlin_brands_str + '\n    )\n\n    val countries: List<EncCountry> = listOf('
)

with open('app/src/main/java/com/example/autoelectricai/data/encyclopedia/EncyclopediaCatalog.kt', 'w', encoding='utf-8') as f:
    f.write(content)
