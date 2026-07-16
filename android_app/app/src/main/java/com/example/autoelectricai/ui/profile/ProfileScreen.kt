package com.example.autoelectricai.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.theme.*
import com.example.autoelectricai.ui.settings.apiKeyTextFieldColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    emailOverride: String? = null,
    onBack: (() -> Unit)? = null,
    onNavigateToAwards: (() -> Unit)? = null,
    onNavigateToBookmarks: (() -> Unit)? = null,
    onNavigateToModeration: (() -> Unit)? = null,
    onNavigateToLeaderboard: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val email by viewModel.userEmail.collectAsStateWithLifecycle()
    val role by viewModel.userRole.collectAsStateWithLifecycle()
    val karma by viewModel.userKarma.collectAsStateWithLifecycle()
    val awards by viewModel.userAwards.collectAsStateWithLifecycle()
    val displayedAwards by viewModel.displayedAwards.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isOwnProfile by viewModel.isOwnProfile.collectAsStateWithLifecycle()
    val nicknameError by viewModel.nicknameError.collectAsStateWithLifecycle()
    val isLoadingNickname by viewModel.isLoadingNickname.collectAsStateWithLifecycle()
    val isLoadingAuth by viewModel.isLoadingAuth.collectAsStateWithLifecycle()

    var selectedAward by remember { mutableStateOf<AwardInfo?>(null) }
    var nicknameInput by remember { mutableStateOf("") }
    var adminEmailInput by remember { mutableStateOf("") }
    val adminActionMessage by viewModel.adminActionMessage.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(emailOverride) {
        viewModel.loadProfile(emailOverride)
    }

    val roleName = when (role) {
        "admin" -> "Создатель (Admin)"
        "master" -> "Мастер-Диагност"
        "specialist" -> "Специалист"
        "contributor" -> "Практикант"
        else -> "Новичок"
    }

    val roleColor = when (role) {
        "admin" -> ErrorRed
        "master" -> Color(0xFFFF5252)
        "specialist" -> AmberPrimary
        "contributor" -> SuccessGreen
        else -> TextHint
    }

    Scaffold(
        containerColor = DarkBackground
    ) { padding ->
        if (isLoadingAuth) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AmberPrimary)
            }
        } else if (email.isBlank() && onBack == null) {
            Box(modifier = Modifier.padding(padding)) {
                com.example.autoelectricai.ui.auth.AuthScreen(
                    onAuthSuccess = {
                        viewModel.loadProfile(null)
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (email.isBlank()) {
                    Text("Профиль не найден или вы не авторизованы.", color = TextPrimary)
                } else {
                    if (onBack != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
                            }
                            Text("Профиль автора", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Никнейм Setup
                    if (isOwnProfile && userName == null) {
                        if (isLoadingNickname) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AmberPrimary)
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AmberPrimary.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Придумайте никнейм", color = AmberPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Никнейм увидят другие пользователи в Энциклопедии. Его можно установить только один раз!", color = TextSecondary, fontSize = 12.sp)
                                    OutlinedTextField(
                                        value = nicknameInput,
                                        onValueChange = { nicknameInput = it },
                                        placeholder = { Text("Например: VAG_Master", color = TextHint) },
                                        singleLine = true,
                                        isError = nicknameError != null,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AmberPrimary,
                                            unfocusedBorderColor = TextHint,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (nicknameError != null) {
                                        Text(nicknameError!!, color = ErrorRed, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.setNickname(nicknameInput) },
                                        enabled = nicknameInput.length >= 3,
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                                    ) {
                                        Text("Сохранить навсегда", color = DarkBackground)
                                    }
                                }
                            }
                        }
                    }

                    // Bookmarks Card (Offline Sandbox)
                    if (isOwnProfile && onNavigateToBookmarks != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToBookmarks() },
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(AmberPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = AmberPrimary)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Закладки и Мои решения", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Ваша офлайн-песочница решений", color = TextHint, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // User Info Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(DarkSurface2, RoundedCornerShape(40.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val initial = userName?.firstOrNull()?.uppercase() ?: email.firstOrNull()?.uppercase() ?: "?"
                                Text(initial, fontSize = 40.sp, color = AmberPrimary, fontWeight = FontWeight.Bold)
                            }
                            Text(userName ?: "Без имени", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(email, color = TextHint, fontSize = 14.sp)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Роль", color = TextSecondary, fontSize = 12.sp)
                                    Text(roleName, color = roleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Карма", color = TextSecondary, fontSize = 12.sp)
                                    Text("★ $karma", color = AmberPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    // Awards Section
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Достижения профиля", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (isOwnProfile && onNavigateToAwards != null) {
                            TextButton(onClick = onNavigateToAwards) {
                                Text("Изменить", color = AmberPrimary)
                            }
                        }
                    }
                    
                    if (displayedAwards.isEmpty()) {
                        Text("У вас пока нет достижений для отображения. Помогайте сообществу, публикуя полезные решения, чтобы заработать медали!", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            displayedAwards.forEach { award ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(DarkSurface2, RoundedCornerShape(20.dp)) // Круглая медалька
                                        .clickable { selectedAward = award },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(painterResource(id = award.iconResId), contentDescription = award.title, tint = AmberPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Moderation Queue (For Specialists and Masters)
                    if (isOwnProfile && onNavigateToModeration != null && (role == "specialist" || role == "admin" || role == "master")) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Зона Специалиста", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                                Text("У вас есть права на модерацию решений практикантов.", color = TextSecondary, fontSize = 13.sp)
                                Button(
                                    onClick = onNavigateToModeration,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PendingActions, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Очередь модерации", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Leaderboard (Top Experts) - Available to all users
                    if (onNavigateToLeaderboard != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EmojiEvents, null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Таблица Лидеров", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                                Text("Узнайте, кто из диагностов внес наибольший вклад в базу знаний.", color = TextSecondary, fontSize = 13.sp)
                                Button(
                                    onClick = onNavigateToLeaderboard,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Leaderboard, null, tint = AmberPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Смотреть Топ-10", color = AmberPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Admin Panel (Only for Master)
                    if (isOwnProfile && (role == "admin" || role == "master")) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Панель управления (Admin)", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                                Text("Выдача прав доступа экспертам", color = TextSecondary, fontSize = 13.sp)
                                
                                OutlinedTextField(
                                    value = adminEmailInput,
                                    onValueChange = { adminEmailInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Email пользователя", color = TextHint, fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    colors = apiKeyTextFieldColors(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.grantRole(adminEmailInput.trim(), "contributor") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface2),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Практикант", color = TextPrimary, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.grantRole(adminEmailInput.trim(), "specialist") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface2),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Специалист", color = TextPrimary, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.grantRole(adminEmailInput.trim(), "admin") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface2),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Мастер", color = Color.Red, fontSize = 11.sp)
                                    }
                                }
                                
                                Button(
                                    onClick = { viewModel.revokeRole(adminEmailInput.trim()) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331111)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Забрать все права (сделать новичком)", color = Color.Red, fontSize = 12.sp)
                                }

                                if (adminActionMessage != null) {
                                    Text(
                                        text = adminActionMessage ?: "",
                                        color = Color.Green,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Test button
                    if (isOwnProfile && email == "j.j.niccals2@gmail.com") {
                        Button(
                            onClick = { viewModel.grantAllAwardsToSelf() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Text("DEV: Получить все награды", color = Color.White)
                        }
                    }

                    if (isOwnProfile) {
                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Выйти из аккаунта", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    selectedAward?.let { award ->
        AlertDialog(
            onDismissRequest = { selectedAward = null },
            icon = { Icon(painterResource(id = award.iconResId), contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(48.dp)) },
            title = { Text(award.title, color = TextPrimary, textAlign = TextAlign.Center) },
            text = { Text(award.description, color = TextSecondary, textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(onClick = { selectedAward = null }) {
                    Text("Отлично", color = AmberPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
