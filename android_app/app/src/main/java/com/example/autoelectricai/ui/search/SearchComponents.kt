package com.example.autoelectricai.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.data.db.DtcEntity
import com.example.autoelectricai.theme.*

@Composable
fun DtcSearchResultCard(
    dtc: DtcEntity,
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
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (dtc.severity) {
                            "critical" -> ErrorRed.copy(alpha = 0.2f)
                            "warning" -> WarningOrange.copy(alpha = 0.2f)
                            else -> LocalHitBlue.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dtc.code,
                    color = when (dtc.severity) {
                        "critical" -> ErrorRed
                        "warning" -> WarningOrange
                        else -> LocalHitBlue
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    dtc.descriptionRu,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Text(
                    dtc.system.replaceFirstChar { it.uppercase() },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DiagnosisSearchResultCard(
    entity: DiagnosisEntity,
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
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmberPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Build,
                    null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row {
                    Text(
                        "${entity.carBrand} ${entity.carModel}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (entity.system.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "• ${entity.system}",
                            color = AmberPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    entity.symptoms.take(80) + if (entity.symptoms.length > 80) "..." else "",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
            if (entity.successCount > 0) {
                Text(
                    "✓ ${entity.successCount}",
                    color = SuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
