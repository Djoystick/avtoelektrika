package com.example.autoelectricai.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var expandedItem by remember { mutableStateOf<Long?>(null) }
    var showDeleteDialog by remember { mutableStateOf<DiagnosisEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить решение?", color = TextPrimary) },
            text = { Text("Это действие нельзя отменить.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBookmark(showDeleteDialog!!)
                    showDeleteDialog = null
                }) {
                    Text("Удалить", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Закладки и Мои решения", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AmberPrimary)
            }
        } else if (bookmarks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("У вас пока нет сохраненных решений", color = TextHint, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                items(bookmarks, key = { it.id }) { entity ->
                    BookmarkCard(
                        entity = entity,
                        isExpanded = expandedItem == entity.id,
                        onClick = { expandedItem = if (expandedItem == entity.id) null else entity.id },
                        onDeleteClick = { showDeleteDialog = entity },
                        dateFormat = dateFormat
                    )
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    entity: DiagnosisEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (entity.isOfflineReady) {
                        Icon(Icons.Default.WifiOff, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LocalHitBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (entity.source == "ai_generated") entity.aiProvider.uppercase() else "ЛОКАЛЬНО",
                            color = LocalHitBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    dateFormat.format(Date(entity.createdAt)),
                    color = TextHint,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(8.dp))

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

            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DarkSurface2, modifier = Modifier.padding(bottom = 10.dp))

                    if (entity.errorCodes.isNotBlank()) {
                        Text("Коды ошибок", color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(entity.errorCodes, color = ErrorRed, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                    
                    Text("Симптомы", color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(entity.symptoms.ifBlank { "Не указаны" }, color = TextPrimary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    Text("Решение", color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    val formattedSolution = try {
                        val gson = com.google.gson.Gson()
                        val cleaned = entity.solution.replace("```json", "").replace("```", "").trim()
                        val parsed = gson.fromJson(cleaned, com.example.autoelectricai.data.ai.DiagnosisResponse::class.java)
                        if (parsed != null && parsed.solutions.isNotEmpty()) {
                            parsed.solutions.joinToString(separator = "\n\n") { solution ->
                                val t = if (solution.title.isBlank()) "" else "🔹 ${solution.title}\n"
                                val desc = if (solution.description.isNullOrBlank()) "" else "${solution.description}\n"
                                val stps = if (solution.steps.isEmpty()) "" else solution.steps.joinToString("\n") { "• $it" }
                                "$t$desc$stps".trim()
                            }
                        } else {
                            entity.solution
                        }
                    } catch (e: Exception) {
                        entity.solution
                    }
                    Text(formattedSolution, color = SuccessGreen, fontSize = 13.sp)
                    
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}
