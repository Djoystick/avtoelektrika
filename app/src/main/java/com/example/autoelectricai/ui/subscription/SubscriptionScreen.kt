package com.example.autoelectricai.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoelectricai.theme.*

// Contact link for manual subscription purchase
private const val TELEGRAM_CONTACT = "https://t.me/your_telegram_here"

@Composable
fun SubscriptionScreen(
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(DarkSurface, DarkBackground)
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = TextSecondary)
                }
                Spacer(Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Участник проекта",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Откройте доступ к полной базе знаний\nот реальных автоэлектриков",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Comparison table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Сравнение тарифов",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            FeatureRow(
                label = "ИИ-диагностика",
                free = true,
                paid = true,
                icon = Icons.Default.Psychology
            )
            FeatureRow(
                label = "Закладки и офлайн-режим",
                free = true,
                paid = true,
                icon = Icons.Default.BookmarkBorder
            )
            FeatureRow(
                label = "Синхронизация своих решений",
                free = true,
                paid = true,
                icon = Icons.Default.Sync
            )
            FeatureRow(
                label = "Глобальная база сообщества",
                free = false,
                paid = true,
                icon = Icons.Default.Public
            )
            FeatureRow(
                label = "Голосование за решения",
                free = false,
                paid = true,
                icon = Icons.Default.ThumbUp
            )
            FeatureRow(
                label = "Приоритетная поддержка",
                free = false,
                paid = true,
                icon = Icons.Default.SupportAgent
            )
        }

        Spacer(Modifier.height(24.dp))

        // Paid card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(2.dp, AmberPrimary, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Участник проекта", fontWeight = FontWeight.Bold, color = AmberPrimary, fontSize = 18.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Единоразовый платёж — бессрочный доступ",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Цена по запросу",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Свяжитесь с нами для получения доступа",
                    fontSize = 12.sp,
                    color = TextHint,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { uriHandler.openUri(TELEGRAM_CONTACT) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Написать в Telegram", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Free tier reminder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Бесплатный доступ", fontWeight = FontWeight.Bold, color = TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Базовые функции ИИ-диагностики, закладки и синхронизация ваших решений — бесплатно и навсегда.",
                    fontSize = 12.sp,
                    color = TextHint,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureRow(
    label: String,
    free: Boolean,
    paid: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        // Free column
        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            if (free) {
                Icon(Icons.Default.Check, contentDescription = "Да", tint = SuccessGreen, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.Close, contentDescription = "Нет", tint = ErrorRed.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
        }
        // Paid column
        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            if (paid) {
                Icon(Icons.Default.Check, contentDescription = "Да", tint = AmberPrimary, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.Close, contentDescription = "Нет", tint = ErrorRed.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
        }
    }
    HorizontalDivider(color = DarkSurface, thickness = 0.5.dp)
}
