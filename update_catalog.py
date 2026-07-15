import re

with open('app/src/main/java/com/example/autoelectricai/data/encyclopedia/EncyclopediaCatalog.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update EncBrand
content = content.replace('val logoResId: Int,', 'val logoResId: Int?,')

# 2. Update EncPlatform
content = content.replace('val icon: String,', 'val icon: String,\n    val startYear: Int,\n    val endYear: Int?,')

# 3. Update EncPlatform instances. They look like: EncPlatform("id", "Name", "📂", listOf(
# We want to insert startYear=2000, endYear=2024 right after the icon.
# Using regex: EncPlatform\("([^"]+)", "([^"]+)", "([^"]+)", listOf\(
# To: EncPlatform("\1", "\2", "\3", 2000, 2024, listOf(
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
# find where countries list ends and insert
content = content.replace(
    'EncCountry("usa", "США", "🇺🇸", brandsFlat.filter { it.id in listOf("ford", "chevrolet") })',
    'EncCountry("usa", "США", "🇺🇸", brandsFlat.filter { it.id in listOf("ford", "chevrolet", "jeep", "dodge", "chrysler", "cadillac", "tesla") }),' + countries_str
)

with open('app/src/main/java/com/example/autoelectricai/data/encyclopedia/EncyclopediaCatalog.kt', 'w', encoding='utf-8') as f:
    f.write(content)
