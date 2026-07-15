import re

with open('app/src/main/java/com/example/autoelectricai/ui/knowledgebase/KnowledgeBaseScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add selectedYear state
content = content.replace(
    'var selectedSubsystem by remember { mutableStateOf<String?>(null) }',
    'var selectedSubsystem by remember { mutableStateOf<String?>(null) }\n    var selectedYear by remember { mutableStateOf<Int?>(null) }'
)

# 2. Reset selectedYear on brand selection
content = content.replace(
    'selectedPlatform = null\n                                    selectedSystem = null\n                                    selectedSubsystem = null\n                                    nav = EncNav.PLATFORMS',
    'selectedPlatform = null\n                                    selectedSystem = null\n                                    selectedSubsystem = null\n                                    selectedYear = null\n                                    nav = EncNav.PLATFORMS'
)

# 3. Add Filter row to PLATFORMS view
old_platforms_view = '''                    // ─── PLATFORMS LIST ───────────────────────────────────────
                    EncNav.PLATFORMS -> {
                        val brand = selectedBrand ?: return@AnimatedContent
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(brand.platforms) { platform ->'''

new_platforms_view = '''                    // ─── PLATFORMS LIST ───────────────────────────────────────
                    EncNav.PLATFORMS -> {
                        val brand = selectedBrand ?: return@AnimatedContent
                        
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Year Filter
                            val years = listOf(null, 1990, 1995, 2000, 2005, 2010, 2015, 2020, 2025)
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(years) { year ->
                                    val isSelected = selectedYear == year
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) brand.primaryColor else Color.DarkGray)
                                            .clickable { selectedYear = year }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = year?.toString() ?: "Все",
                                            color = if (isSelected) brand.secondaryColor else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            
                            val filteredPlatforms = brand.platforms.filter { p -> 
                                val sy = selectedYear
                                if (sy == null) true else sy >= p.startYear && (p.endYear == null || sy <= p.endYear!!)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredPlatforms) { platform ->'''

content = content.replace(old_platforms_view, new_platforms_view)

# close the column for Platforms List
content = content.replace(
    'item { Spacer(Modifier.height(60.dp)) }\n                        }\n                    }',
    'item { Spacer(Modifier.height(60.dp)) }\n                            }\n                        }\n                    }'
)

# 4. Fix BrandCard
old_brand_card_image = '''                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = brand.logoResId),
                    contentDescription = brand.shortName,
                    modifier = Modifier.fillMaxSize()
                )'''

new_brand_card_image = '''                if (brand.logoResId != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = brand.logoResId!!),
                        contentDescription = brand.shortName,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(brand.primaryColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = brand.shortName.take(3).uppercase(),
                            color = brand.secondaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }'''

content = content.replace(old_brand_card_image, new_brand_card_image)

with open('app/src/main/java/com/example/autoelectricai/ui/knowledgebase/KnowledgeBaseScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
