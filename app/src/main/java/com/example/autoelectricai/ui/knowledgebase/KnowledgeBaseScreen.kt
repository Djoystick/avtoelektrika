package com.example.autoelectricai.ui.knowledgebase

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class EncyclopediaLevel {
    BRANDS, MODELS, SYSTEMS, SOLUTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(
    onAuthorClick: (String) -> Unit = {},
    viewModel: KnowledgeBaseViewModel = hiltViewModel()
) {
    val diagnoses by viewModel.communityDiagnoses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    var navState by remember { mutableStateOf(EncyclopediaLevel.BRANDS) }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var selectedSystem by remember { mutableStateOf<String?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var addendumEntity by remember { mutableStateOf<DiagnosisEntity?>(null) }
    var addendumText by remember { mutableStateOf("") }
    var expandedItem by remember { mutableStateOf<Long?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }

    // Intercept back button to navigate up the drill-down hierarchy
    BackHandler(enabled = navState != EncyclopediaLevel.BRANDS) {
        when (navState) {
            EncyclopediaLevel.SOLUTIONS -> navState = EncyclopediaLevel.SYSTEMS
            EncyclopediaLevel.SYSTEMS -> navState = EncyclopediaLevel.MODELS
            EncyclopediaLevel.MODELS -> navState = EncyclopediaLevel.BRANDS
            EncyclopediaLevel.BRANDS -> {}
        }
    }

    if (addendumEntity != null) {
        AlertDialog(
            onDismissRequest = { addendumEntity = null },
            title = { Text("Внести дополнение", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = addendumText,
                    onValueChange = { addendumText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ваше дополнение", color = TextHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = DarkSurface2,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addendumText.isNotBlank()) {
                        viewModel.appendAddendum(addendumEntity!!, addendumText)
                    }
                    addendumEntity = null
                    addendumText = ""
                }) {
                    Text("Отправить", color = AmberPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { addendumEntity = null }) {
                    Text("Отмена", color = TextSecondary)
                }
            },
            containerColor = DarkSurface2
        )
    }

    if (showPaywall) {
        AlertDialog(
            onDismissRequest = { showPaywall = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(48.dp)) },
            title = { Text("Доступно участникам", color = TextPrimary, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "Глобальная база проверенных решений от реальных автоэлектриков открыта только для Участников проекта. \n\nВы можете просматривать каталог, но доступ к самим решениям скрыт.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPaywall = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Понятно", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (navState) {
                        EncyclopediaLevel.BRANDS -> "Энциклопедия"
                        EncyclopediaLevel.MODELS -> selectedBrand ?: "Модели"
                        EncyclopediaLevel.SYSTEMS -> selectedModel ?: "Системы"
                        EncyclopediaLevel.SOLUTIONS -> selectedSystem ?: "Решения"
                    }
                    Text(titleText, color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    if (navState != EncyclopediaLevel.BRANDS) {
                        IconButton(onClick = {
                            when (navState) {
                                EncyclopediaLevel.SOLUTIONS -> navState = EncyclopediaLevel.SYSTEMS
                                EncyclopediaLevel.SYSTEMS -> navState = EncyclopediaLevel.MODELS
                                EncyclopediaLevel.MODELS -> navState = EncyclopediaLevel.BRANDS
                                else -> {}
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
                        }
                    } else {
                        // Empty icon space to keep alignment
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AmberPrimary)
            }
        } else if (diagnoses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Глобальная база пока пуста", color = TextHint, fontSize = 16.sp)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Breadcrumbs
                if (navState != EncyclopediaLevel.BRANDS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface2)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "База",
                            color = TextHint,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { navState = EncyclopediaLevel.BRANDS }
                        )
                        if (navState >= EncyclopediaLevel.MODELS && selectedBrand != null) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextHint, modifier = Modifier.size(16.dp))
                            Text(
                                selectedBrand!!,
                                color = if (navState == EncyclopediaLevel.MODELS) AmberPrimary else TextHint,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { navState = EncyclopediaLevel.MODELS }
                            )
                        }
                        if (navState >= EncyclopediaLevel.SYSTEMS && selectedModel != null) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextHint, modifier = Modifier.size(16.dp))
                            Text(
                                selectedModel!!,
                                color = if (navState == EncyclopediaLevel.SYSTEMS) AmberPrimary else TextHint,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { navState = EncyclopediaLevel.SYSTEMS }
                            )
                        }
                    }
                }

                // Content
                AnimatedContent(
                    targetState = navState,
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
                    },
                    label = "encyclopedia_nav"
                ) { state ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (state) {
                            EncyclopediaLevel.BRANDS -> {
                                val brands = diagnoses.map { it.carBrand.ifBlank { "Без марки" } }.distinct().sorted()
                                items(brands) { brand ->
                                    CategoryItem(
                                        title = brand,
                                        icon = Icons.Default.DirectionsCar,
                                        onClick = {
                                            selectedBrand = brand
                                            navState = EncyclopediaLevel.MODELS
                                        }
                                    )
                                }
                            }
                            EncyclopediaLevel.MODELS -> {
                                val models = diagnoses
                                    .filter { it.carBrand == selectedBrand || (selectedBrand == "Без марки" && it.carBrand.isBlank()) }
                                    .map { it.carModel.ifBlank { "Без модели" } }
                                    .distinct()
                                    .sorted()
                                items(models) { model ->
                                    CategoryItem(
                                        title = model,
                                        icon = Icons.Default.Settings,
                                        onClick = {
                                            selectedModel = model
                                            navState = EncyclopediaLevel.SYSTEMS
                                        }
                                    )
                                }
                            }
                            EncyclopediaLevel.SYSTEMS -> {
                                val systems = diagnoses
                                    .filter { 
                                        (it.carBrand == selectedBrand || (selectedBrand == "Без марки" && it.carBrand.isBlank())) &&
                                        (it.carModel == selectedModel || (selectedModel == "Без модели" && it.carModel.isBlank()))
                                    }
                                    .map { it.system.ifBlank { "Прочее" } }
                                    .distinct()
                                    .sorted()
                                items(systems) { system ->
                                    val count = diagnoses.count {
                                        (it.carBrand == selectedBrand || (selectedBrand == "Без марки" && it.carBrand.isBlank())) &&
                                        (it.carModel == selectedModel || (selectedModel == "Без модели" && it.carModel.isBlank())) &&
                                        (it.system == system || (system == "Прочее" && it.system.isBlank()))
                                    }
                                    CategoryItem(
                                        title = system,
                                        subtitle = "$count решений",
                                        icon = Icons.Default.Memory,
                                        onClick = {
                                            if (currentUserRole == "user") {
                                                showPaywall = true
                                            } else {
                                                selectedSystem = system
                                                navState = EncyclopediaLevel.SOLUTIONS
                                            }
                                        }
                                    )
                                }
                            }
                            EncyclopediaLevel.SOLUTIONS -> {
                                val solutionsList = diagnoses.filter {
                                    (it.carBrand == selectedBrand || (selectedBrand == "Без марки" && it.carBrand.isBlank())) &&
                                    (it.carModel == selectedModel || (selectedModel == "Без модели" && it.carModel.isBlank())) &&
                                    (it.system == selectedSystem || (selectedSystem == "Прочее" && it.system.isBlank()))
                                }
                                
                                items(solutionsList, key = { it.cloudId ?: it.id }) { entity ->
                                    SolutionCard(
                                        entity = entity,
                                        isExpanded = expandedItem == entity.id,
                                        onClick = { expandedItem = if (expandedItem == entity.id) null else entity.id },
                                        onAuthorClick = { if (it.isNotBlank()) onAuthorClick(it) },
                                        onLike = { viewModel.voteForSolution(entity, true) },
                                        onDislike = { viewModel.voteForSolution(entity, false) },
                                        onAddAddendum = { addendumEntity = entity },
                                        dateFormat = dateFormat
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkSurface2, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, color = TextHint, fontSize = 12.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun SolutionCard(
    entity: DiagnosisEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onAddAddendum: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    val isAutoParsed = entity.source == "auto_parsed"
    val authorDisplay = if (isAutoParsed) "🤖 AI-Парсер" else (entity.authorUsername?.takeIf { it.isNotBlank() } ?: entity.authorEmail.takeIf { it.isNotBlank() } ?: "Аноним")
    val authorColor = if (isAutoParsed) Color(0xFFCE93D8) else AmberPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAuthorClick(entity.authorEmail) }
                ) {
                    Icon(
                        if (isAutoParsed) Icons.Default.SmartToy else Icons.Default.Person,
                        contentDescription = null,
                        tint = authorColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(authorDisplay, color = authorColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(dateFormat.format(Date(entity.createdAt)), color = TextHint, fontSize = 12.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text("Симптомы:", color = TextHint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(entity.symptoms.ifBlank { "Не указаны" }, color = TextPrimary, fontSize = 14.sp)

            if (entity.errorCodes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Ошибки:", color = TextHint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(entity.errorCodes, color = ErrorRed, fontSize = 14.sp)
            }

            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DarkSurface2, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Text("Решение:", color = TextHint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(entity.solution, color = SuccessGreen, fontSize = 14.sp)

                    Spacer(Modifier.height(16.dp))

                    // Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Votes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface2)
                        ) {
                            IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.ThumbUp, 
                                    contentDescription = "Like",
                                    tint = if (entity.userVote == "like") SuccessGreen else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text("${entity.likes - entity.dislikes}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            IconButton(onClick = onDislike, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.ThumbDown, 
                                    contentDescription = "Dislike",
                                    tint = if (entity.userVote == "dislike") ErrorRed else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButton(onClick = onAddAddendum) {
                            Icon(Icons.Default.AddComment, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Дополнить", color = AmberPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
