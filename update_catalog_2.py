with open('app/src/main/java/com/example/autoelectricai/data/encyclopedia/EncyclopediaCatalog.kt', 'r', encoding='utf-8') as f:
    content = f.read()

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

with open('app/src/main/java/com/example/autoelectricai/data/encyclopedia/EncyclopediaCatalog.kt', 'w', encoding='utf-8') as f:
    f.write(content)
