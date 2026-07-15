package com.example.autoelectricai.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.theme.*
import com.example.autoelectricai.utils.AppLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToModeration: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    updateViewModel: com.example.autoelectricai.ui.main.AppUpdateViewModel = hiltViewModel()
) {
    val updateInfo by updateViewModel.updateInfo.collectAsStateWithLifecycle()
    val downloadState by updateViewModel.downloadState.collectAsStateWithLifecycle()
    val isCheckingForUpdate by updateViewModel.isChecking.collectAsStateWithLifecycle()
    val hasCheckedForUpdate by updateViewModel.hasChecked.collectAsStateWithLifecycle()

    val geminiKey by viewModel.geminiKey.collectAsStateWithLifecycle()
    val geminiProxyUrl by viewModel.geminiProxyUrl.collectAsStateWithLifecycle()
    val openAiKey by viewModel.openAiKey.collectAsStateWithLifecycle()
    val preferredAi by viewModel.preferredAi.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    var showGeminiKey by remember { mutableStateOf(false) }
    var showOpenAiKey by remember { mutableStateOf(false) }
    var showChangelog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val isExpertLoggedIn by viewModel.isExpertLoggedIn.collectAsStateWithLifecycle()
    val expertEmail by viewModel.expertEmail.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val userKarma by viewModel.userKarma.collectAsStateWithLifecycle()
    val adminActionMessage by viewModel.adminActionMessage.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    var adminEmailInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AmberPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Настройки", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Settings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Настройки ИИ", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    // Preferred AI selector
                    Text("Основной провайдер ИИ", color = TextSecondary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiProviderButton(
                            label = "Gemini",
                            subtitle = "Google",
                            selected = preferredAi == "gemini",
                            onClick = { viewModel.setPreferredAi("gemini") },
                            modifier = Modifier.weight(1f)
                        )
                        AiProviderButton(
                            label = "Claude 4.6",
                            subtitle = "Dellmar",
                            selected = preferredAi == "openai",
                            onClick = { viewModel.setPreferredAi("openai") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        "Резервный провайдер активируется автоматически при ошибке основного.",
                        color = TextHint,
                        fontSize = 11.sp
                    )

                    Divider(color = DividerColor)

                    var showApiKeys by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showApiKeys = !showApiKeys }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("API Ключи и Proxy", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Icon(if (showApiKeys) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = TextSecondary)
                    }

                    AnimatedVisibility(visible = showApiKeys) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Dellmar Key
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, null, tint = LocalHitBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Dellmar API (Claude 4.6)", color = TextPrimary, fontSize = 14.sp)
                                }
                                OutlinedTextField(
                                    value = openAiKey,
                                    onValueChange = { viewModel.setOpenAiKey(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("API ключ (sk-cvc...)", color = TextHint, fontSize = 12.sp) },
                                    placeholder = { Text("sk-cvc...", color = TextHint, fontSize = 13.sp) },
                                    visualTransformation = if (showOpenAiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showOpenAiKey = !showOpenAiKey }) {
                                            Icon(if (showOpenAiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = apiKeyTextFieldColors(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Gemini Key
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Google Gemini API", color = TextPrimary, fontSize = 14.sp)
                                }
                                OutlinedTextField(
                                    value = geminiKey,
                                    onValueChange = { viewModel.setGeminiKey(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("API ключ Gemini", color = TextHint, fontSize = 12.sp) },
                                    placeholder = { Text("AIza...", color = TextHint, fontSize = 13.sp) },
                                    visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                            Icon(if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = apiKeyTextFieldColors(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                @OptIn(ExperimentalMaterial3Api::class)
                                var proxyExpanded by remember { mutableStateOf(false) }
                                val presetProxies = listOf(
                                    "https://generativelanguage.googleapis.com/",
                                    "https://api.gemini.dev/",
                                    "https://gemini.kks-lab.com/",
                                    "https://api.jsq.app/",
                                    "https://google.aiproxy.dev/",
                                    "https://ai-proxy.workers.dev/"
                                )
                                
                                ExposedDropdownMenuBox(
                                    expanded = proxyExpanded,
                                    onExpandedChange = { proxyExpanded = !proxyExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = geminiProxyUrl,
                                        onValueChange = { viewModel.setGeminiProxyUrl(it) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        label = { Text("Proxy URL для Gemini", color = TextHint, fontSize = 12.sp) },
                                        placeholder = { Text("https://generativelanguage.googleapis.com/", color = TextHint, fontSize = 13.sp) },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = proxyExpanded)
                                        },
                                        colors = apiKeyTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = proxyExpanded,
                                        onDismissRequest = { proxyExpanded = false },
                                        containerColor = DarkSurface2
                                    ) {
                                        presetProxies.forEach { proxy ->
                                            DropdownMenuItem(
                                                text = { Text(proxy, color = TextPrimary, fontSize = 13.sp) },
                                                onClick = {
                                                    viewModel.setGeminiProxyUrl(proxy)
                                                    proxyExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Text("Получить бесплатный ключ: aistudio.google.com", color = TextHint, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Save button
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (saved) Icons.Default.CheckCircle else Icons.Default.Save, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (saved) "Сохранено!" else "Сохранить настройки",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Expert Login Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Для экспертов", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(if (isExpertLoggedIn) "В сети" else "Гость", color = if (isExpertLoggedIn) Color.Green else TextHint, fontSize = 11.sp)
                    }

                    if (isExpertLoggedIn) {
                        Text("Вы авторизованы как:", color = TextSecondary, fontSize = 13.sp)
                        Text(expertEmail, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        
                        // Show Karma
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Карма: $userKarma", color = AmberPrimary, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { onNavigateToProfile() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Мой Профиль", color = AmberPrimary)
                        }
                    } else {
                        Text("Авторизуйтесь, чтобы добавлять свои решения в глобальную базу.", color = TextHint, fontSize = 13.sp)
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email", color = TextHint, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = apiKeyTextFieldColors(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = passInput,
                            onValueChange = { passInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Пароль", color = TextHint, fontSize = 12.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = apiKeyTextFieldColors(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        if (authError != null) {
                            Text(authError ?: "", color = Color.Red, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.loginExpert(emailInput.trim(), passInput.trim()) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isAuthLoading && emailInput.isNotBlank() && passInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isAuthLoading) "Вход..." else "Войти", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Moderation Queue (For Specialists and Masters)
            if (userRole == "specialist" || userRole == "master") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
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
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
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

            // Admin Panel (Only for Master)
            if (userRole == "admin") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
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

            // About App Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("О приложении", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("АвтоЭлектрик AI v${com.example.autoelectricai.BuildConfig.VERSION_NAME}", color = TextPrimary, fontSize = 14.sp)
                        TextButton(onClick = { showChangelog = true }) {
                            Text("История изменений", color = AmberPrimary, fontSize = 13.sp)
                        }
                    }
                    Button(
                        onClick = { viewModel.forceSync() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface2),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AmberPrimary, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Синхронизация...", color = AmberPrimary, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Синхронизировать БД (Тест)", color = AmberPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = {
                            val logFile = AppLogger.getLogFile()
                            if (logFile != null && logFile.exists()) {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", logFile)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Экспорт логов"))
                            } else {
                                Toast.makeText(context, "Файл логов пуст или не найден", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Экспорт логов", color = AmberPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "Ключи API хранятся только на вашем устройстве.\nДанные не передаются третьим лицам.",
                        color = TextHint,
                        fontSize = 11.sp
                    )
                }
            }

            // App Update Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    com.example.autoelectricai.ui.main.UpdateSection(
                        updateInfo = updateInfo,
                        downloadState = downloadState,
                        isChecking = isCheckingForUpdate,
                        hasChecked = hasCheckedForUpdate,
                        onCheckForUpdates = { updateViewModel.checkForUpdates() },
                        onStartDownload = { updateViewModel.startDownload() },
                        onCancelDownload = { updateViewModel.cancelDownload() }
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showChangelog) {
        ChangelogDialog(onDismiss = { showChangelog = false })
    }
}

@Composable
fun ChangelogDialog(onDismiss: () -> Unit) {
    val changelog = listOf(
        "v1.7.15" to "Fix missing Google Auth Client ID and Telegram auth visibility issues",
        "v1.7.15" to "Fix missing Google Auth Client ID and Telegram auth visibility issues",
        "v1.7.14" to "Fix AuthScreen visibility bug for unauthenticated users",
        "v1.7.13" to "Fix AuthScreen visibility bug",
        "v1.7.13" to "Fix AuthScreen visibility bug",
        "v1.7.13" to "Hotfix: Исправлена ошибка FileProvider при установке обновления.",
        "v1.7.12" to "Hotfix: Исправлена ошибка загрузки обновлений (Flow Invariant Violation).",
        "v1.7.11" to "Тестовое обновление: Проверка новой системы ручной проверки обновлений.",
        "v1.7.10" to "Hotfix: Исправлен критический сбой при запуске приложения на старых устройствах.",
        "v1.7.9" to "Автоматизированная система обновлений через GitHub Releases. Больше никакой рутины! 🎉",
        "v1.7.8 [Hotfix]" to "Исправлен краш приложения при сбросе формы (Новый поиск). Исправлена логика БД: теперь анонимные пользователи могут отправлять решения на модерацию.",
        "v1.7.7 [Патч]" to "Исправлен сетевой таймаут ожидания для тяжелых ИИ моделей. Теперь подробные ответы (до 4000 токенов) успешно загружаются, а красивые UI-спойлеры работают корректно.",
        "v1.7.6 [Патч]" to "Увеличен лимит токенов генерации (до 4000) для предотвращения обрывов текста. Исправлен баг, из-за которого длинные инструкции от ИИ не отображались под спойлерами. Добавлена строгая валидация JSON ответов.",
        "v1.7.5 [Патч]" to "UI/UX улучшения: 'Новый поиск' возвращает на начальный экран, исправлен баг с клавиатурой, перекрывающей поле ввода года, оптимизирован дизайн шапки Настроек.",
        "v1.7.4 [Патч]" to "Оптимизация сети: добавлены кастомные Proxy для Gemini, переработаны таймауты и внедрена быстрая модель claude-sonnet-4.6 для Dellmar.",
        "v1.7.3 [Минорное]" to "Кэширование никнейма в локальной памяти, исправление UI загрузки (анимация радара не обрезается), индикатор загрузки профиля.",
        "v1.7.2 [Минорное]" to "Стабилизация Авторизации: полностью переработан механизм отслеживания сессии, предотвращающий появление 'зомби-аккаунтов'. Отключено автоматическое резервное копирование Android, устранив проблему потери ключей.",
        "v1.7.1 [Минорное]" to "Прогресс Диагностики: визуальная полоска прогресса (90-секундный таймер) со стадиями. Значительно доработан системный промпт ИИ — генерация подробных пошаговых атомарных инструкций. Исправление бага сортировки базы данных и внедрение бесплатного безлимитного провайдера Pollinations AI в качестве резервного ИИ. Улучшена детализация логов диагностики.",
        "v1.7.0 [Мажорное]" to "Разделение Базы Знаний: Внедрены вкладки 'Мои решения' и 'Решения сообщества'. Новая Система Достижений (10 наград в стиле RPG). Добавлена возможность вносить Дополнения (Addendums) к чужим диагнозам. Обновлена лестница Кармы и Модерации.",
        "v1.6.2 [Минорное]" to "Исправлено отображение роли Администратора ('Создатель') в Профиле.",
        "v1.6.1 [Минорное]" to "Исправлен баг, из-за которого при выдаче роли 'admin' стирались данные профиля и слетал никнейм. Добавлена возможность вернуть свой старый никнейм.",
        "v1.6.0 [Мажорное]" to "Внедрен доступ Администратора с панелью управления ролями в Настройках. Стандартные иконки наград заменены на красивые медали из FontAwesome. Добавлен экран Управления Достижениями с возможностью выбора 7 главных наград для показа в профиле.",
        "v1.5.2 [Минорное]" to "Гарантированная загрузка решений в облако (отправка вынесена в фоновый процесс) и исправление бага с ложной ошибкой 'никнейм занят' при отсутствии интернета.",
        "v1.5.1 [Минорное]" to "Исправлено дублирование решений при скачивании обновлений сообщества и устранен баг с двойной накруткой лайков/дизлайков.",
        "v1.5.0 [Мажорное]" to "Добавлена система уникальных никнеймов и 17 новых достижений в Профиль. Имя автора в Энциклопедии теперь кликабельно и ведет на его профиль. Обновлена иконка приложения.",
        "v1.4.0 [Мажорное]" to "Глобальный редизайн навигации: внедрена нижняя панель (Bottom Navigation Bar) с 3 вкладками (Диагностика, Энциклопедия, Профиль).",
        "v1.3.4 [Минорное]" to "Профили и Награды (Этап 4): Добавлен экран профиля с кубками, локальное голосование и кнопка принудительной синхронизации БД.",
        "v1.3.3 [Минорное]" to "Этап 3 (Геймификация и Карма): Добавлена система Кармы (+50 за аппрув, +10 за лайк). Добавлена Таблица Лидеров (Топ-10).",
        "v1.3.2 [Минорное]" to "Этап 2 (Очередь модерации): Добавлена Зона Специалиста. Теперь эксперты могут просматривать ожидающие решения практикантов, одобрять их (публиковать в глобальную базу) или отклонять.",
        "v1.3.1 [Минорное]" to "Внедрен 'Этап 1' системы ролей (RBAC). Добавлена секретная Панель Администратора для Мастеров-Диагностов с возможностью выдачи прав другим экспертам. Публикация в глобальную базу теперь ограничена по ролям.",
        "v1.3.0 [Мажорное]" to "🔥 Глобальное облачное обновление!\n• Подключена облачная синхронизация (Firebase).\n• Добавлена система рейтингов (Лайк/Дизлайк) для лучших решений от сообщества.\n• Внедрен 'Режим Эксперта' с авторизацией по Email для публикации диагнозов.\n• Фоновая загрузка топ-200 решений для работы в офлайне.\n• Новый бейдж '🌐 От сообщества' для облачных диагнозов.",
        "v1.2.3" to "Добавлена классная 3D-подобная анимация ИИ-сканирования (в стиле радар/Lottie) во время поиска решения.",
        "v1.2.2" to "Добавлен тактильный отклик (Haptic Feedback) на основные элементы управления для ощущения премиальности.",
        "v1.2.1" to "Добавлен голосовой ввод симптомов (Speech-to-Text). Теперь можно диктовать проблему в микрофон.",
        "v1.2.0" to "Полный редизайн интерфейса, добавление функции 'Мой гараж' для быстрого выбора недавних авто, пошаговый мастер (Wizard) диагностики, умные подсказки симптомов (Pill-кнопки).",
        "v1.1.8" to "Улучшена детализация ответов ИИ (структура подробной энциклопедии) и добавлено красивое форматирование сохраненных диагнозов в Базе знаний.",
        "v1.1.7" to "Свернутый вид списка изменений (аккордеон) для удобного чтения истории версий.",
        "v1.1.6" to "Умная структура ответов ИИ: компактный UI с аккордеонами и возможность сохранять только полезные шаги в базу.",
        "v1.1.5" to "Переход на мощную модель GPT-5.5 через API Dellmar. Теперь это основной провайдер по умолчанию.",
        "v1.1.4" to "Добавлено логирование и экспорт логов для отладки.",
        "v1.1.3" to "Встроены дефолтные и резервные API-ключи (Gemini, OpenRouter, Groq). Пользователям больше не обязательно вводить свои ключи.",
        "v1.1.2" to "Значительно расширена база марок и моделей (добавлены JDM, китайские авто, коммерческий транспорт и др.).",
        "v1.1.1" to "Добавлены УАЗ и ГАЗ в список марок автомобилей.",
        "v1.1" to "Добавлен каскадный выпадающий список (Марка -> Модель -> Год) в стиле auto.ru.\nВнутренние улучшения и исправление конфликтов библиотек.",
        "v1.0" to "Первый релиз. Базовый функционал ИИ-диагностики, локальная база знаний, поддержка Gemini и OpenAI."
    )

    var expandedVersion by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("История изменений", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                changelog.forEach { (version, desc) ->
                    val isExpanded = expandedVersion == version
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            expandedVersion = if (isExpanded) null else version
                        },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(version, color = AmberPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextHint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(animationSpec = tween(300)),
                                exit = shrinkVertically(animationSpec = tween(300))
                            ) {
                                Column {
                                    Spacer(Modifier.height(8.dp))
                                    Text(desc, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = AmberPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AiProviderButton(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) AmberPrimary else DarkSurface2
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color = if (selected) Color.Black else TextPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
            Text(
                subtitle,
                color = if (selected) Color.Black.copy(alpha = 0.7f) else TextHint,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun apiKeyTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AmberPrimary,
    unfocusedBorderColor = DividerColor,
    focusedLabelColor = AmberPrimary,
    unfocusedLabelColor = TextHint,
    cursorColor = AmberPrimary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    unfocusedContainerColor = DarkSurface2,
    focusedContainerColor = DarkSurface2
)
