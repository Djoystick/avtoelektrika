package com.example.autoelectricai.ui.diagnosis

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.autoelectricai.data.db.RecentCar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.theme.*
import com.google.gson.Gson
import com.example.autoelectricai.data.ai.DiagnosisResponse
import com.example.autoelectricai.data.ai.SolutionBlock
import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.autoelectricai.utils.VoiceToTextParser

val AUTO_SYSTEMS = listOf(
    "Электропроводка",
    "Система зажигания",
    "Аккумулятор / Зарядка",
    "Генератор / Стартер",
    "Освещение (фары, габариты)",
    "Центральный замок / Окна",
    "Датчики / OBD",
    "Сигнализация",
    "Мультимедиа / Магнитола",
    "Предохранители / Реле",
    "Климат-контроль",
    "ABS / ESP",
    "Прочее"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(
    viewModel: DiagnosisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val carBrand by viewModel.carBrand.collectAsStateWithLifecycle()
    val carModel by viewModel.carModel.collectAsStateWithLifecycle()
    val carYear by viewModel.carYear.collectAsStateWithLifecycle()
    val selectedSystem by viewModel.selectedSystem.collectAsStateWithLifecycle()
    val symptoms by viewModel.symptoms.collectAsStateWithLifecycle()
    val errorCodes by viewModel.errorCodes.collectAsStateWithLifecycle()
    val brands by viewModel.brands.collectAsStateWithLifecycle()
    val availableModels by viewModel.availableModels.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showObdInput by remember { mutableStateOf(errorCodes.isNotBlank()) }
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()

    BackHandler(enabled = currentStep != DiagnosisStep.CAR || uiState is DiagnosisUiState.Success || uiState is DiagnosisUiState.Error) {
        viewModel.prevStep()
    }

    val context = LocalContext.current

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Surface(
                color = AmberPrimary.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/c/3707633620/1"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore if no browser/telegram
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alpha",
                        tint = AmberPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alpha-версия приложения. При возникновении ошибок, пожалуйста, экспортируйте лог в Настройках и отправьте нам.",
                        color = AmberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    ) { padding ->
        val stateIndex = when {
            uiState is DiagnosisUiState.Success || uiState is DiagnosisUiState.Error -> 3
            currentStep == DiagnosisStep.SYMPTOMS -> 2
            currentStep == DiagnosisStep.SYSTEM -> 1
            else -> 0
        }

        AnimatedContent(
            targetState = stateIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut()
                } else {
                    slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut()
                }
            },
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            label = "wizard"
        ) { index ->
            when (index) {
                0 -> {
                    val recentCars by viewModel.recentCars.collectAsStateWithLifecycle()
                    StepCar(
                        carBrand = carBrand,
                        carModel = carModel,
                        carYear = carYear,
                        brands = brands,
                        availableModels = availableModels,
                        availableYears = availableYears,
                        recentCars = recentCars,
                        viewModel = viewModel
                    )
                }
                1 -> StepSystem(
                    selectedSystem = selectedSystem,
                    onSelect = { viewModel.selectedSystem.value = it },
                    onNext = { viewModel.nextStep() }
                )
                2 -> StepSymptoms(
                    selectedSystem = selectedSystem,
                    symptoms = symptoms,
                    errorCodes = errorCodes,
                    suggestions = suggestions,
                    uiState = uiState,
                    showObdInput = showObdInput,
                    onShowObdInputChanged = { showObdInput = it },
                    viewModel = viewModel,
                    focusManager = focusManager
                )
                3 -> StepResult(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun StepCar(
    carBrand: String,
    carModel: String,
    carYear: String,
    brands: List<String>,
    availableModels: List<String>,
    availableYears: List<String>,
    recentCars: List<RecentCar>,
    viewModel: DiagnosisViewModel
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (recentCars.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                SectionTitle(title = "Мой гараж", icon = Icons.Default.DirectionsCar)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    items(recentCars) { car ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface2)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectRecentCar(car)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column {
                                Text(car.brand, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${car.model} ${car.year}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            SectionTitle(title = if (recentCars.isNotEmpty()) "Или введите новый автомобиль" else "Какой у вас автомобиль?", icon = Icons.Default.Edit)
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AutoCompleteField(
                    value = carBrand,
                    onValueChange = { 
                        viewModel.carBrand.value = it 
                        if (!brands.contains(it)) viewModel.carModel.value = "" 
                    },
                    label = "Марка",
                    suggestions = if (carBrand.isEmpty()) brands else brands.filter { it.contains(carBrand, ignoreCase = true) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                AutoCompleteField(
                    value = carModel,
                    onValueChange = { viewModel.carModel.value = it },
                    label = "Модель",
                    suggestions = if (carModel.isEmpty()) availableModels else availableModels.filter { it.contains(carModel, ignoreCase = true) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                AutoCompleteField(
                    value = carYear,
                    onValueChange = { viewModel.carYear.value = it },
                    label = "Год выпуска",
                    suggestions = if (carYear.isEmpty()) availableYears else availableYears.filter { it.contains(carYear, ignoreCase = true) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.nextStep()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                shape = RoundedCornerShape(50)
            ) {
                Text("Далее", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun StepSystem(
    selectedSystem: String,
    onSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            SectionTitle(title = "Выберите систему", icon = Icons.Default.AccountTree)
            SystemSelector(
                selected = selectedSystem,
                onSelect = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(it)
                }
            )
            
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNext()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedSystem.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, disabledContainerColor = DarkSurface2),
                shape = RoundedCornerShape(50)
            ) {
                Text("Далее", color = if (selectedSystem.isNotBlank()) Color.Black else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepSymptoms(
    selectedSystem: String,
    symptoms: String,
    errorCodes: String,
    suggestions: List<DiagnosisEntity>,
    uiState: DiagnosisUiState,
    showObdInput: Boolean,
    onShowObdInputChanged: (Boolean) -> Unit,
    viewModel: DiagnosisViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val voiceParser = remember { VoiceToTextParser(context) }
    val voiceState by voiceParser.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            voiceParser.startListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose { voiceParser.stopListening() }
    }

    LaunchedEffect(voiceState.isSpeaking) {
        if (!voiceState.isSpeaking && voiceState.spokenText.isNotBlank()) {
            val current = viewModel.symptoms.value
            val prefix = if (current.isBlank()) "" else "$current, "
            viewModel.onSymptomsChanged(prefix + voiceState.spokenText)
            voiceParser.reset()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            SectionTitle(title = "Что случилось?", icon = Icons.Default.AutoAwesome)
            
            val predefined = com.example.autoelectricai.data.CommonSymptoms.predefinedSymptoms[selectedSystem] ?: emptyList()
            if (predefined.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    predefined.forEach { symptom ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkSurface2)
                                .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onSymptomsChanged(if (symptoms.isBlank()) symptom else "$symptoms, $symptom") 
                            }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(symptom, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = if (voiceState.isSpeaking && voiceState.spokenText.isNotBlank()) {
                    val current = viewModel.symptoms.value
                    if (current.isBlank()) voiceState.spokenText else "$current, ${voiceState.spokenText}"
                } else symptoms,
                onValueChange = { viewModel.onSymptomsChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (voiceState.isSpeaking) "Слушаю вас..." else "Или опишите проблему своими словами...", color = if (voiceState.isSpeaking) AmberPrimary else TextHint, fontSize = 14.sp) },
                minLines = 3,
                maxLines = 6,
                colors = appTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (voiceState.isSpeaking) AmberPrimary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable {
                                if (voiceState.isSpeaking) {
                                    voiceParser.stopListening()
                                } else {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        voiceParser.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (voiceState.isSpeaking) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Голосовой ввод",
                            tint = if (voiceState.isSpeaking) AmberPrimary else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            
            AnimatedVisibility(visible = voiceState.error != null) {
                Text(voiceState.error ?: "", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
            
            AnimatedVisibility(visible = !showObdInput) {
                TextButton(onClick = { onShowObdInputChanged(true) }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Добавить коды ошибок OBD", color = AmberPrimary)
                }
            }

            AnimatedVisibility(visible = showObdInput) {
                OutlinedTextField(
                    value = errorCodes,
                    onValueChange = { viewModel.errorCodes.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Коды ошибок (P0301, B1234...)", color = TextHint, fontSize = 12.sp) },
                    colors = appTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }

            AnimatedVisibility(visible = suggestions.isNotEmpty()) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Найдено в базе:",
                        color = LocalHitBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    suggestions.forEach { entity ->
                        SuggestionChip(entity = entity, onClick = {
                            focusManager.clearFocus()
                            viewModel.selectSuggestion(entity)
                        })
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            val gradient = Brush.horizontalGradient(listOf(AmberPrimary, AmberDark))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    focusManager.clearFocus()
                    viewModel.diagnose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .height(if (uiState is DiagnosisUiState.Loading) 240.dp else 56.dp),
                enabled = uiState !is DiagnosisUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState is DiagnosisUiState.Loading) Color.Transparent else AmberPrimary,
                    disabledContainerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(if (uiState is DiagnosisUiState.Loading) 24.dp else 50.dp)
            ) {
                if (uiState is DiagnosisUiState.Loading) {
                    var progress by remember { mutableStateOf(0f) }
                    var stageText by remember { mutableStateOf("Подготовка запроса...") }

                    LaunchedEffect(Unit) {
                        progress = 0f
                        val stages = listOf(
                            0f to "Инициализация ИИ-агента...",
                            0.1f to "Анализ симптомов и кодов ошибок...",
                            0.3f to "Поиск в базах данных Service Manual...",
                            0.5f to "Формирование диагностических гипотез...",
                            0.7f to "Генерация подробной пошаговой инструкции...",
                            0.9f to "Финализация ответа..."
                        )
                        
                        var elapsed = 0L
                        val maxTime = 90000L // 90 seconds expected time for heavy AI models
                        while (elapsed < maxTime) {
                            kotlinx.coroutines.delay(100)
                            elapsed += 100
                            val currentRatio = elapsed.toFloat() / maxTime
                            // Ease out progress towards 95%
                            progress = currentRatio * 0.95f
                            
                            val currentStage = stages.last { progress >= it.first }
                            stageText = currentStage.second
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AiScanAnimation()
                        Spacer(Modifier.height(12.dp))
                        Text(stageText, color = AmberPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AmberPrimary,
                            trackColor = Color.DarkGray
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("${(progress * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Найти решение", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun StepResult(
    uiState: DiagnosisUiState,
    viewModel: DiagnosisViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val state = uiState) {
            is DiagnosisUiState.Success -> {
                item {
                    Spacer(Modifier.height(16.dp))
                    ResultCard(
                        entity = state.entity,
                        isLocal = state.isLocal,
                        provider = state.provider,
                        isSaved = state.isSaved,
                        onMarkSuccess = { selectedIndices -> viewModel.markAsSuccessful(state.entity.id, selectedIndices) },
                        onVote = { isLike -> state.entity.cloudId?.let { cloudId -> viewModel.voteForSolution(cloudId, state.entity.id, isLike) } },
                        onReset = { viewModel.reset() }
                    )
                    Spacer(Modifier.height(40.dp))
                }
            }
            is DiagnosisUiState.Error -> {
                item {
                    Spacer(Modifier.height(16.dp))
                    ErrorCard(message = state.message, onDismiss = { viewModel.reset() })
                    Spacer(Modifier.height(40.dp))
                }
            }
            else -> {}
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AmberPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AmberPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text(label, color = TextHint, fontSize = 12.sp) },
            colors = appTextFieldColors(),
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            containerColor = DarkSurface2
        ) {
            suggestions.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s, color = TextPrimary) },
                    onClick = { onValueChange(s); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemSelector(selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AUTO_SYSTEMS.forEach { system ->
            val isSelected = system == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) AmberPrimary.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) AmberPrimary else DividerColor,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelect(if (isSelected) "" else system) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    system,
                    color = if (isSelected) AmberPrimary else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(entity: DiagnosisEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface2),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Storage, null, tint = LocalHitBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entity.symptoms.take(80) + if (entity.symptoms.length > 80) "..." else "",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (entity.solution.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Уже есть решение", color = SuccessGreen, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    entity: DiagnosisEntity,
    isLocal: Boolean,
    provider: String,
    isSaved: Boolean,
    onMarkSuccess: (Set<Int>) -> Unit,
    onVote: (Boolean) -> Unit,
    onReset: () -> Unit
) {
    var parsedResponse by remember(entity.solution) { mutableStateOf<DiagnosisResponse?>(null) }
    var isParsing by remember(entity.solution) { mutableStateOf(true) }
    
    LaunchedEffect(entity.solution) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            try {
                val cleaned = entity.solution.replace("```json", "").replace("```", "").replace("\"stens\":", "\"steps\":").trim()
                val gson = com.google.gson.GsonBuilder().setLenient().create()
                parsedResponse = gson.fromJson(cleaned, DiagnosisResponse::class.java)
            } catch (e: Exception) {
                parsedResponse = null
            } finally {
                isParsing = false
            }
        }
    }
    
    val steps = remember(entity.solution) { parseSteps(entity.solution) }
    val checkedSteps = remember { mutableStateMapOf<Int, Boolean>() }
    val checkedBlocks = remember { mutableStateMapOf<Int, Boolean>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLocal) LocalHitBlue.copy(alpha = 0.2f) else AmberPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isLocal) Icons.Default.Storage else Icons.Default.AutoAwesome,
                            null,
                            tint = if (isLocal) LocalHitBlue else AmberPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isLocal) "Из базы данных" else "AI: ${provider.uppercase()}",
                            color = if (isLocal) LocalHitBlue else AmberPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (entity.isFromCommunity) {
                    Text("🌐 От сообщества", color = LocalHitBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else if (entity.successCount > 0) {
                    Text("✓ ${entity.successCount}×", color = SuccessGreen, fontSize = 12.sp)
                }
            }

            Divider(color = DividerColor)

            // Content
            if (isParsing) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = AmberPrimary, strokeWidth = 2.dp)
                }
            } else if (parsedResponse != null && parsedResponse!!.solutions.isNotEmpty()) {
                parsedResponse!!.solutions.forEachIndexed { index, solution ->
                    SolutionBlockCard(
                        index = index + 1,
                        solution = solution,
                        checked = checkedBlocks[index] == true,
                        onCheckChanged = { checkedBlocks[index] = it }
                    )
                }
            } else if (steps.isNotEmpty()) {
                steps.forEachIndexed { index, step ->
                    StepRow(
                        number = index + 1,
                        text = step,
                        checked = checkedSteps[index] == true,
                        onCheck = { checkedSteps[index] = !checkedSteps.getOrDefault(index, false) }
                    )
                }
            } else {
                // Plain text fallback with JSON cleaning
                val fallback = if (entity.solution.trim().startsWith("{")) {
                    entity.solution.replace(Regex("[\"\\{\\}\\[\\]]"), "")
                        .replace("solutions:", "")
                        .replace("title:", "\n📌 ")
                        .replace("description:", "\n📝 ")
                        .replace("steps:", "\n🛠 ")
                        .replace("stens:", "\n🛠 ")
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() && it != "," }
                        .joinToString("\n")
                } else entity.solution
                Text(fallback, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)
            }

            Divider(color = DividerColor)

            // Voting block (for community solutions)
            if (entity.isFromCommunity) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Помогло ли вам это решение?", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.width(16.dp))
                    IconButton(
                        onClick = { onVote(true) },
                        enabled = entity.userVote == null
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = "Лайк",
                            tint = if (entity.userVote == "like") SuccessGreen else TextHint
                        )
                    }
                    Text("${entity.likes}", color = if (entity.userVote == "like") SuccessGreen else TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.width(16.dp))
                    IconButton(
                        onClick = { onVote(false) },
                        enabled = entity.userVote == null
                    ) {
                        Icon(
                            Icons.Default.ThumbDown,
                            contentDescription = "Дизлайк",
                            tint = if (entity.userVote == "dislike") ErrorRed else TextHint
                        )
                    }
                    Text("${entity.dislikes}", color = if (entity.userVote == "dislike") ErrorRed else TextSecondary, fontSize = 14.sp)
                }
                Divider(color = DividerColor)
            }

            // Action buttons
            if (!isSaved) {
                Button(
                    onClick = {
                        val response = parsedResponse
                        if (response != null && response.solutions.isNotEmpty()) {
                            onMarkSuccess(checkedBlocks.filterValues { it }.keys)
                        } else {
                            onMarkSuccess(emptySet())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("✅ Помогло! Сохранить в базу", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Сохранено в базу данных", color = SuccessGreen, fontWeight = FontWeight.Medium)
                }
            }

            TextButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Новый запрос", color = TextSecondary)
            }
        }
    }
}

@Composable
fun SolutionBlockCard(index: Int, solution: SolutionBlock, checked: Boolean, onCheckChanged: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) SuccessGreen.copy(alpha = 0.1f) else DarkSurface2
        ),
        shape = RoundedCornerShape(10.dp),
        border = if (checked) BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)) else null
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (checked) SuccessGreen else AmberPrimary.copy(alpha = 0.2f))
                        .clickable { onCheckChanged(!checked) },
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(index.toString(), color = AmberPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    solution.title,
                    color = if (checked) SuccessGreen else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(start = 48.dp, end = 12.dp, bottom = 12.dp)) {
                    Divider(color = DividerColor, modifier = Modifier.padding(bottom = 8.dp))
                    solution.steps.forEachIndexed { stepIndex, stepText ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Text(
                                "${stepIndex + 1}.",
                                color = TextHint,
                                fontSize = 13.sp,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                stepText,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepRow(number: Int, text: String, checked: Boolean, onCheck: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) SuccessGreen.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onCheck)
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) SuccessGreen else AmberPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(number.toString(), color = AmberPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            color = if (checked) TextSecondary else TextPrimary,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ErrorCard(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Ошибка", color = ErrorRed, fontWeight = FontWeight.Bold)
                Text(message, color = TextSecondary, fontSize = 13.sp)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = TextSecondary)
            }
        }
    }
}

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AmberPrimary,
    unfocusedBorderColor = DividerColor,
    focusedLabelColor = AmberPrimary,
    unfocusedLabelColor = TextHint,
    cursorColor = AmberPrimary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent
)

fun parseSteps(text: String): List<String> {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val stepPattern = Regex("^(\\d+)[.)\\s]+(.+)")
    val steps = mutableListOf<String>()
    var currentStep = StringBuilder()

    for (line in lines) {
        val match = stepPattern.find(line)
        if (match != null) {
            if (currentStep.isNotEmpty()) steps.add(currentStep.toString().trim())
            currentStep = StringBuilder(match.groupValues[2])
        } else {
            if (currentStep.isNotEmpty()) currentStep.append(" ").append(line)
        }
    }
    if (currentStep.isNotEmpty()) steps.add(currentStep.toString().trim())
    return if (steps.size >= 2) steps else emptyList()
}

@Composable
fun AiScanAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(pulse)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                // Outer ring
                drawCircle(
                    color = AmberPrimary.copy(alpha = 0.2f),
                    radius = size.width / 2,
                    style = Stroke(strokeWidth)
                )
                // Inner ring
                drawCircle(
                    color = AmberPrimary.copy(alpha = 0.4f),
                    radius = size.width / 3,
                    style = Stroke(strokeWidth)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
                .drawBehind {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0.0f to Color.Transparent,
                            0.8f to AmberPrimary.copy(alpha = 0.1f),
                            1.0f to AmberPrimary
                        ),
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = true,
                        size = size
                    )
                }
        )
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AmberPrimary,
            modifier = Modifier.size(32.dp)
        )
    }
}
