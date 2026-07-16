package com.example.autoelectricai.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoelectricai.data.db.DtcEntity
import com.example.autoelectricai.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DtcDetailScreen(
    dtc: DtcEntity,
    onBack: () -> Unit
) {
    val causes: List<String> = try {
        Gson().fromJson(dtc.commonCauses, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    val fixes: List<String> = try {
        Gson().fromJson(dtc.commonFixes, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    val relatedCodes = dtc.relatedCodes.split(",").filter { it.isNotBlank() }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(dtc.code, color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Severity badge
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (dtc.severity) {
                                    "critical" -> ErrorRed.copy(alpha = 0.2f)
                                    "warning" -> WarningOrange.copy(alpha = 0.2f)
                                    else -> LocalHitBlue.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            when (dtc.severity) {
                                "critical" -> "Критично"
                                "warning" -> "Внимание"
                                else -> "Инфо"
                            },
                            color = when (dtc.severity) {
                                "critical" -> ErrorRed
                                "warning" -> WarningOrange
                                else -> LocalHitBlue
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        dtc.category,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    if (dtc.system.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "• ${dtc.system}",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Description
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Описание", color = AmberPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(dtc.descriptionRu, color = TextPrimary, fontSize = 16.sp, lineHeight = 24.sp)
                        if (dtc.descriptionEn.isNotBlank() && dtc.descriptionEn != dtc.descriptionRu) {
                            Spacer(Modifier.height(6.dp))
                            Text(dtc.descriptionEn, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Common causes
            if (causes.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Типичные причины", color = WarningOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            causes.forEach { cause ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text("• ", color = WarningOrange, fontSize = 14.sp)
                                    Text(cause, color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Common fixes
            if (fixes.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Решения", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            fixes.forEach { fix ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text("✓ ", color = SuccessGreen, fontSize = 14.sp)
                                    Text(fix, color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Related codes
            if (relatedCodes.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Связанные коды", color = LocalHitBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(relatedCodes) { code ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(code.trim(), fontSize = 12.sp) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = DarkSurface2,
                                            labelColor = LocalHitBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}
