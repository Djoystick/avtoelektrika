package com.example.autoelectricai.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.theme.*

@Composable
fun QuickCards(
    popularDiagnoses: List<DiagnosisEntity>,
    recentDiagnoses: List<DiagnosisEntity>,
    onDiagnosisClick: (DiagnosisEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        if (popularDiagnoses.isNotEmpty()) {
            QuickCardSection(
                title = "Популярное сегодня",
                icon = Icons.Default.TrendingUp,
                iconColor = AmberPrimary,
                items = popularDiagnoses,
                onDiagnosisClick = onDiagnosisClick
            )
            Spacer(Modifier.height(16.dp))
        }

        if (recentDiagnoses.isNotEmpty()) {
            QuickCardSection(
                title = "Недавно добавлено",
                icon = Icons.Default.NewReleases,
                iconColor = LocalHitBlue,
                items = recentDiagnoses,
                onDiagnosisClick = onDiagnosisClick
            )
        }
    }
}

@Composable
private fun QuickCardSection(
    title: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    items: List<DiagnosisEntity>,
    onDiagnosisClick: (DiagnosisEntity) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items.take(5), key = { it.cloudId ?: it.id }) { entity ->
                QuickCardItem(entity = entity, onClick = { onDiagnosisClick(entity) })
            }
        }
    }
}

@Composable
private fun QuickCardItem(
    entity: DiagnosisEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "${entity.carBrand} ${entity.carModel}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))

            if (entity.system.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmberPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        entity.system.take(15),
                        color = AmberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            Text(
                entity.symptoms.take(40) + if (entity.symptoms.length > 40) "..." else "",
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            if (entity.successCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${entity.successCount}× помогло",
                        color = SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
