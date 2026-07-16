package com.example.autoelectricai.ui.moderation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    onBack: () -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val pendingSolutions by viewModel.pendingSolutions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on action
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Group solutions by brand
    val grouped = pendingSolutions.groupBy { it.carBrand.ifBlank { "Без марки" } }.toSortedMap()

    // Track which brands are expanded
    val expandedBrands = remember { mutableStateMapOf<String, Boolean>() }
    // Track which item is expanded for detail view
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    // Edit dialog state
    var editTarget by remember { mutableStateOf<DiagnosisEntity?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Очередь модерации", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "${pendingSolutions.size} решений ожидает",
                            color = TextHint,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPendingSolutions() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = AmberPrimary)
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
        } else if (pendingSolutions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Очередь пуста!", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Все решения обработаны 🎉", color = TextHint, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Stats header
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBadge(
                            label = "Всего",
                            value = "${pendingSolutions.size}",
                            color = AmberPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            label = "Марок",
                            value = "${grouped.size}",
                            color = Color(0xFF64B5F6),
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            label = "Авто-парсер",
                            value = "${pendingSolutions.count { it.source == "auto_parsed" }}",
                            color = Color(0xFFBA68C8),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Grouped by brand
                grouped.forEach { (brand, solutions) ->
                    val isExpanded = expandedBrands[brand] != false // default expanded

                    item(key = "brand_$brand") {
                        BrandGroupHeader(
                            brand = brand,
                            count = solutions.size,
                            isExpanded = isExpanded,
                            onClick = { expandedBrands[brand] = !isExpanded }
                        )
                    }

                    if (isExpanded) {
                        items(solutions, key = { it.cloudId ?: it.id.toString() }) { entity ->
                            AnimatedVisibility(
                                visible = true,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                ModerationCard(
                                    entity = entity,
                                    isExpanded = expandedItemId == (entity.cloudId ?: entity.id.toString()),
                                    onClick = {
                                        val key = entity.cloudId ?: entity.id.toString()
                                        expandedItemId = if (expandedItemId == key) null else key
                                    },
                                    onApprove = { viewModel.approveSolution(entity.cloudId ?: "") },
                                    onReject = { viewModel.rejectSolution(entity.cloudId ?: "") },
                                    onEdit = { editTarget = entity }
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }

    // Edit dialog
    editTarget?.let { target ->
        EditAndApproveDialog(
            entity = target,
            onDismiss = { editTarget = null },
            onConfirm = { editedSolution, editedSymptoms ->
                viewModel.approveWithEdit(target.cloudId ?: "", editedSolution, editedSymptoms)
                editTarget = null
            }
        )
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextHint, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BrandGroupHeader(
    brand: String,
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(brand, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Badge(containerColor = AmberPrimary.copy(alpha = 0.2f)) {
            Text("$count", color = AmberPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ModerationCard(
    entity: DiagnosisEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit
) {
    // Determine source label
    val isAutoParsed = entity.source == "auto_parsed"
    val aiScore = entity.aiConfidenceScore
    val aiScoreColor = when {
        aiScore == null -> TextHint
        aiScore >= 75 -> SuccessGreen
        aiScore >= 45 -> Color(0xFFFFA726)
        else -> ErrorRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isExpanded) 1.dp else 0.dp,
                color = if (isExpanded) AmberPrimary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header row: status + source + AI score
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Source badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isAutoParsed) Color(0xFF4A148C).copy(0.3f) else Color(0xFF1A237E).copy(0.3f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isAutoParsed) "🤖 Авто-парсер" else "👤 Пользователь",
                        color = if (isAutoParsed) Color(0xFFCE93D8) else Color(0xFF90CAF9),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.weight(1f))

                // AI Score — visible only to experts/admins (already filtered at nav level)
                if (aiScore != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(aiScoreColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = aiScoreColor, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "ИИ: $aiScore%",
                                color = aiScoreColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                "${entity.carBrand} ${entity.carModel} ${entity.carYear}".trim(),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Система: ${entity.system.ifBlank { "Не указана" }}",
                color = TextSecondary,
                fontSize = 13.sp
            )

            // Author info
            if (entity.authorEmail.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextHint, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(entity.authorUsername ?: entity.authorEmail, color = TextHint, fontSize = 11.sp)
                }
            }

            // Expanded details
            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DarkSurface2, modifier = Modifier.padding(bottom = 10.dp))

                    if (entity.errorCodes.isNotBlank()) {
                        DetailSection(label = "Коды ошибок", value = entity.errorCodes, valueColor = ErrorRed)
                        Spacer(Modifier.height(8.dp))
                    }
                    DetailSection(label = "Симптомы", value = entity.symptoms.ifBlank { "Не указаны" }, valueColor = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    DetailSection(label = "Решение", value = entity.solution, valueColor = SuccessGreen)
                    
                    if (entity.aiConfidenceReason != null) {
                        Spacer(Modifier.height(8.dp))
                        DetailSection(label = "Вердикт ИИ", value = entity.aiConfidenceReason, valueColor = aiScoreColor)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action buttons
                    if (entity.cloudId != null) {
                        // Primary row: Reject + Approve
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Отклонить", fontSize = 13.sp)
                            }
                            Button(
                                onClick = onApprove,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Одобрить", color = SuccessGreen, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Secondary: Edit + Approve
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Редактировать и одобрить", fontSize = 13.sp)
                        }
                    } else {
                        Text(
                            "⚠️ cloudId отсутствует — модерация недоступна",
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(label: String, value: String, valueColor: Color) {
    Text(label, color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(2.dp))
    Text(value, color = valueColor, fontSize = 13.sp, lineHeight = 18.sp)
}

@Composable
private fun EditAndApproveDialog(
    entity: DiagnosisEntity,
    onDismiss: () -> Unit,
    onConfirm: (solution: String, symptoms: String) -> Unit
) {
    var solution by remember { mutableStateOf(entity.solution) }
    var symptoms by remember { mutableStateOf(entity.symptoms) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Редактировать решение",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Text(
                    "${entity.carBrand} ${entity.carModel} ${entity.carYear}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Симптомы", color = TextHint) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = DarkSurface2,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    minLines = 2,
                    maxLines = 4
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = solution,
                    onValueChange = { solution = it },
                    label = { Text("Решение", color = TextHint) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = DarkSurface2,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    minLines = 3,
                    maxLines = 6
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = { onConfirm(solution, symptoms) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                        enabled = solution.isNotBlank()
                    ) {
                        Text("Одобрить", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
