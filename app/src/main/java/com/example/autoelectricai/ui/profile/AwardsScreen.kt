package com.example.autoelectricai.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AwardsScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val unlockedAwards by viewModel.userAwards.collectAsStateWithLifecycle()
    val displayedAwards by viewModel.displayedAwards.collectAsStateWithLifecycle()

    var tempDisplayedIds by remember { mutableStateOf(displayedAwards.map { it.id }.toSet()) }

    var selectedAward by remember { mutableStateOf<AwardInfo?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Управление Достижениями", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            Surface(color = DarkSurface, tonalElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Выбрано: ${tempDisplayedIds.size} / 7",
                        color = if (tempDisplayedIds.size > 7) ErrorRed else TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.saveDisplayedAwards(tempDisplayedIds.toList())
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                        enabled = tempDisplayedIds.size <= 7
                    ) {
                        Text("Сохранить", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Здесь собраны все возможные достижения АвтоЭлектрик AI. Выберите до 7 штук для отображения в вашем профиле.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AVAILABLE_AWARDS.forEach { award ->
                    val isUnlocked = unlockedAwards.any { it.id == award.id }
                    val isSelected = tempDisplayedIds.contains(award.id)

                    val backgroundColor = if (isUnlocked) DarkSurface2 else DarkSurface
                    val iconTint = if (isUnlocked) AmberPrimary else TextHint
                    val alpha = if (isUnlocked) 1f else 0.4f

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .alpha(alpha)
                            .background(backgroundColor, RoundedCornerShape(36.dp)) // Круглая
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) AmberPrimary else Color.Transparent,
                                shape = RoundedCornerShape(36.dp)
                            )
                            .clickable {
                                if (isUnlocked) {
                                    val newSet = tempDisplayedIds.toMutableSet()
                                    if (newSet.contains(award.id)) {
                                        newSet.remove(award.id)
                                    } else {
                                        newSet.add(award.id)
                                    }
                                    tempDisplayedIds = newSet
                                } else {
                                    selectedAward = award
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(id = award.iconResId), contentDescription = award.title, tint = iconTint, modifier = Modifier.size(36.dp))
                        
                        if (isSelected) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Выбрано",
                                tint = SuccessGreen,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .background(DarkBackground, RoundedCornerShape(10.dp))
                            )
                        }
                    }
                }
            }
        }
    }

    selectedAward?.let { award ->
        AlertDialog(
            onDismissRequest = { selectedAward = null },
            icon = { Icon(painterResource(id = award.iconResId), contentDescription = null, tint = TextHint, modifier = Modifier.size(48.dp)) },
            title = { Text(award.title, color = TextPrimary, textAlign = TextAlign.Center) },
            text = { Text(award.description + "\n\n(Вы еще не получили это достижение)", color = TextSecondary, textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(onClick = { selectedAward = null }) {
                    Text("Понятно", color = AmberPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
