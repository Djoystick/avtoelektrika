package com.example.autoelectricai.ui.knowledgebase

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.encyclopedia.EncBrand
import com.example.autoelectricai.data.encyclopedia.EncPlatform
import com.example.autoelectricai.data.encyclopedia.EncSystem
import com.example.autoelectricai.data.encyclopedia.EncyclopediaCatalog
import com.example.autoelectricai.theme.*
import java.text.SimpleDateFormat
import java.util.*

private enum class EncNav { BRANDS, PLATFORMS, SYSTEMS, SUBSYSTEMS, ARTICLES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(
    onAuthorClick: (String) -> Unit = {},
    viewModel: KnowledgeBaseViewModel = hiltViewModel()
) {
    val diagnoses by viewModel.communityDiagnoses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    var nav by remember { mutableStateOf(EncNav.BRANDS) }
    var selectedBrand by remember { mutableStateOf<EncBrand?>(null) }
    var selectedPlatform by remember { mutableStateOf<EncPlatform?>(null) }
    var selectedSystem by remember { mutableStateOf<EncSystem?>(null) }
    var selectedSubsystem by remember { mutableStateOf<String?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var addendumEntity by remember { mutableStateOf<DiagnosisEntity?>(null) }
    var addendumText by remember { mutableStateOf("") }
    var expandedItem by remember { mutableStateOf<Long?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }

    fun goBack() = when (nav) {
        EncNav.ARTICLES -> {
            if (selectedBrand == null) nav = EncNav.BRANDS
            else nav = EncNav.SUBSYSTEMS
        }
        EncNav.SUBSYSTEMS -> nav = EncNav.SYSTEMS
        EncNav.SYSTEMS -> nav = EncNav.PLATFORMS
        EncNav.PLATFORMS -> nav = EncNav.BRANDS
        EncNav.BRANDS -> {}
    }

    BackHandler(enabled = nav != EncNav.BRANDS) { goBack() }

    // Addendum Dialog
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
                        focusedBorderColor = AmberPrimary, unfocusedBorderColor = DarkSurface2,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addendumText.isNotBlank()) viewModel.appendAddendum(addendumEntity!!, addendumText)
                    addendumEntity = null; addendumText = ""
                }) { Text("Отправить", color = AmberPrimary) }
            },
            dismissButton = { TextButton(onClick = { addendumEntity = null }) { Text("Отмена", color = TextSecondary) } },
            containerColor = DarkSurface2
        )
    }

    // Paywall Dialog
    if (showPaywall) {
        AlertDialog(
            onDismissRequest = { showPaywall = false },
            icon = { Icon(Icons.Default.Lock, null, tint = AmberPrimary, modifier = Modifier.size(48.dp)) },
            title = { Text("Доступно участникам", color = TextPrimary, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "Глобальная база проверенных решений от реальных автоэлектриков открыта только для Участников проекта.",
                    color = TextSecondary, textAlign = TextAlign.Center, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(onClick = { showPaywall = false }, colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)) {
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
                    val title = when (nav) {
                        EncNav.BRANDS -> "Энциклопедия"
                        EncNav.PLATFORMS -> selectedBrand?.displayName ?: "Платформы"
                        EncNav.SYSTEMS -> selectedPlatform?.displayName ?: "Системы"
                        EncNav.SUBSYSTEMS -> selectedSystem?.displayName ?: "Разделы"
                        EncNav.ARTICLES -> selectedSubsystem ?: "Статьи"
                    }
                    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    if (nav != EncNav.BRANDS) {
                        IconButton(onClick = { goBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Breadcrumb trail
            if (nav != EncNav.BRANDS) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(DarkSurface2)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("База", color = TextHint, fontSize = 11.sp,
                        modifier = Modifier.clickable { nav = EncNav.BRANDS })
                    if (selectedBrand != null) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextHint, modifier = Modifier.size(14.dp))
                        Text(selectedBrand!!.shortName,
                            color = if (nav == EncNav.PLATFORMS) AmberPrimary else TextHint,
                            fontSize = 11.sp, modifier = Modifier.clickable { nav = EncNav.PLATFORMS })
                    }
                    if (selectedPlatform != null && nav >= EncNav.SYSTEMS) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextHint, modifier = Modifier.size(14.dp))
                        Text(selectedPlatform!!.icon, fontSize = 11.sp)
                        Text(selectedPlatform!!.displayName,
                            color = if (nav == EncNav.SYSTEMS) AmberPrimary else TextHint,
                            fontSize = 11.sp, modifier = Modifier.clickable { nav = EncNav.SYSTEMS })
                    }
                    if (selectedSystem != null && nav >= EncNav.SUBSYSTEMS) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextHint, modifier = Modifier.size(14.dp))
                        Text(selectedSystem!!.icon, fontSize = 11.sp)
                        Text(selectedSystem!!.displayName,
                            color = if (nav == EncNav.SUBSYSTEMS) AmberPrimary else TextHint,
                            fontSize = 11.sp, modifier = Modifier.clickable { nav = EncNav.SUBSYSTEMS })
                    }
                }
            }

            AnimatedContent(
                targetState = nav,
                transitionSpec = {
                    if (targetState > initialState)
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    else
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                },
                label = "enc_nav"
            ) { currentNav ->
                when (currentNav) {

                    // ─── BRANDS GRID ──────────────────────────────────────────
                    EncNav.BRANDS -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(EncyclopediaCatalog.brands) { brand ->
                                BrandCard(brand = brand, onClick = {
                                    selectedBrand = brand
                                    selectedPlatform = null
                                    selectedSystem = null
                                    selectedSubsystem = null
                                    nav = EncNav.PLATFORMS
                                })
                            }
                            // Extra tile: "All / Community"
                            item {
                                CommunityCard(onClick = {
                                    selectedBrand = null; selectedPlatform = null
                                    selectedSystem = null; selectedSubsystem = null
                                    nav = EncNav.ARTICLES
                                })
                            }
                        }
                    }

                    // ─── PLATFORMS LIST ───────────────────────────────────────
                    EncNav.PLATFORMS -> {
                        val brand = selectedBrand ?: return@AnimatedContent
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(brand.platforms) { platform ->
                                val count = diagnoses.count {
                                    it.carBrand.contains(brand.shortName, ignoreCase = true) &&
                                    it.encyclopediaPlatform.contains(platform.displayName.take(15), ignoreCase = true)
                                }
                                FolderItem(
                                    icon = platform.icon,
                                    title = platform.displayName,
                                    subtitle = if (count > 0) "$count решений" else null,
                                    accentColor = brand.primaryColor
                                ) {
                                    selectedPlatform = platform
                                    selectedSystem = null
                                    selectedSubsystem = null
                                    nav = EncNav.SYSTEMS
                                }
                            }
                            item { Spacer(Modifier.height(60.dp)) }
                        }
                    }

                    // ─── SYSTEMS LIST ─────────────────────────────────────────
                    EncNav.SYSTEMS -> {
                        val platform = selectedPlatform ?: return@AnimatedContent
                        val brand = selectedBrand ?: return@AnimatedContent
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(platform.systems) { system ->
                                val count = diagnoses.count {
                                    it.carBrand.contains(brand.shortName, ignoreCase = true) &&
                                    it.encyclopediaSystem.contains(system.displayName.take(10), ignoreCase = true)
                                }
                                FolderItem(
                                    icon = system.icon,
                                    title = system.displayName,
                                    subtitle = if (count > 0) "$count решений" else null,
                                    accentColor = brand.primaryColor
                                ) {
                                    selectedSystem = system
                                    selectedSubsystem = null
                                    nav = EncNav.SUBSYSTEMS
                                }
                            }
                            item { Spacer(Modifier.height(60.dp)) }
                        }
                    }

                    // ─── SUBSYSTEMS LIST ──────────────────────────────────────
                    EncNav.SUBSYSTEMS -> {
                        val system = selectedSystem ?: return@AnimatedContent
                        val brand = selectedBrand ?: return@AnimatedContent
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(system.subsystems) { sub ->
                                val count = diagnoses.count {
                                    it.carBrand.contains(brand.shortName, ignoreCase = true) &&
                                    it.encyclopediaSubsystem.contains(sub.take(10), ignoreCase = true)
                                }
                                FolderItem(
                                    icon = "📄",
                                    title = sub,
                                    subtitle = if (count > 0) "$count решений" else null,
                                    accentColor = brand.primaryColor
                                ) {
                                    if (currentUserRole == "novice" || currentUserRole == "user") {
                                        showPaywall = true
                                    } else {
                                        selectedSubsystem = sub
                                        nav = EncNav.ARTICLES
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(60.dp)) }
                        }
                    }

                    // ─── ARTICLES LIST ────────────────────────────────────────
                    EncNav.ARTICLES -> {
                        val articles = if (selectedBrand == null) {
                            diagnoses
                        } else {
                            diagnoses.filter { entity ->
                                val brandMatch = entity.carBrand.contains(selectedBrand!!.shortName, ignoreCase = true)
                                val sysMatch = selectedSystem == null ||
                                    entity.encyclopediaSystem.contains(selectedSystem!!.displayName.take(10), ignoreCase = true)
                                val subMatch = selectedSubsystem == null ||
                                    entity.encyclopediaSubsystem.contains(selectedSubsystem!!.take(10), ignoreCase = true)
                                brandMatch && sysMatch && subMatch
                            }
                        }

                        if (isLoading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AmberPrimary)
                            }
                        } else if (articles.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📂", fontSize = 48.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Раздел пока пуст", color = TextSecondary, fontSize = 16.sp)
                                    Text("Решения появятся после публикации\nавтоэлектриками", color = TextHint,
                                        fontSize = 13.sp, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(articles, key = { it.cloudId ?: it.id }) { entity ->
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
                                item { Spacer(Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── BRAND CARD (4-column grid tile) ─────────────────────────────────────────
@Composable
private fun BrandCard(brand: EncBrand, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = brand.logoResId),
                    contentDescription = brand.shortName,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = brand.shortName,
                color = Color(0xFF1A1A1A),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── COMMUNITY "ALL" CARD ─────────────────────────────────────────────────────
@Composable
private fun CommunityCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface2),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                    .background(AmberPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌐", fontSize = 20.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("Все", color = AmberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

// ─── FOLDER ITEM ──────────────────────────────────────────────────────────────
@Composable
private fun FolderItem(
    icon: String,
    title: String,
    subtitle: String?,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSecondary)
        }
    }
}

// ─── SOLUTION CARD ────────────────────────────────────────────────────────────
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
    val authorDisplay = if (isAutoParsed) "🤖 AI-Парсер"
        else (entity.authorUsername?.takeIf { it.isNotBlank() } ?: entity.authorEmail.takeIf { it.isNotBlank() } ?: "Аноним")
    val authorColor = if (isAutoParsed) Color(0xFFCE93D8) else AmberPrimary

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAuthorClick(entity.authorEmail) }) {
                    Icon(if (isAutoParsed) Icons.Default.SmartToy else Icons.Default.Person,
                        null, tint = authorColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(authorDisplay, color = authorColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(dateFormat.format(Date(entity.createdAt)), color = TextHint, fontSize = 12.sp)
            }

            // Encyclopedia path badge
            if (entity.encyclopediaSystem.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (entity.encyclopediaPlatform.isNotBlank()) {
                        Text(entity.encyclopediaPlatform.take(20), color = LocalHitBlue,
                            fontSize = 10.sp,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                .background(LocalHitBlue.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text(entity.encyclopediaSystem, color = AmberPrimary,
                        fontSize = 10.sp,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(AmberPrimary.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Симптомы:", color = TextHint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(entity.symptoms.ifBlank { "Не указаны" }, color = TextPrimary, fontSize = 14.sp)
            if (entity.errorCodes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Ошибки:", color = TextHint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(entity.errorCodes, color = ErrorRed, fontSize = 14.sp)
            }

            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DarkSurface2, modifier = Modifier.padding(bottom = 12.dp))
                    Text("Решение:", color = TextHint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(entity.solution, color = SuccessGreen, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(DarkSurface2)) {
                            IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ThumbUp, "Like",
                                    tint = if (entity.userVote == "like") SuccessGreen else TextSecondary,
                                    modifier = Modifier.size(16.dp))
                            }
                            Text("${entity.likes - entity.dislikes}", color = TextPrimary, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            IconButton(onClick = onDislike, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ThumbDown, "Dislike",
                                    tint = if (entity.userVote == "dislike") ErrorRed else TextSecondary,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                        TextButton(onClick = onAddAddendum) {
                            Icon(Icons.Default.AddComment, null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Дополнить", color = AmberPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
