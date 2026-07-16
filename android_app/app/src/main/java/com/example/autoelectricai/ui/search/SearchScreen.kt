package com.example.autoelectricai.ui.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoelectricai.data.db.DiagnosisEntity
import com.example.autoelectricai.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onDiagnosisClick: (DiagnosisEntity) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val recentQueries by viewModel.recentQueries.collectAsStateWithLifecycle()
    val selectedDtc by viewModel.selectedDtc.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (selectedDtc != null) {
        DtcDetailScreen(
            dtc = selectedDtc!!,
            onBack = { viewModel.clearDtcSelection() }
        )
        return
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Поиск", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search field
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text("Код ошибки, симптом, марка...", color = TextHint, fontSize = 16.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                Icon(Icons.Default.Close, null, tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = AmberPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Loading
                if (isSearching) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AmberPrimary, strokeWidth = 2.dp)
                        }
                    }
                }

                // No query: show quick chips + recent
                if (query.isBlank() && !isSearching) {
                    // Quick DTC chips
                    item {
                        Text("Быстрый доступ", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("P0300", "P0171", "P0174", "U0100", "P0335", "P0420")) { code ->
                                AssistChip(
                                    onClick = { viewModel.onQueryChanged(code) },
                                    label = { Text(code, fontSize = 13.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = DarkSurface2,
                                        labelColor = LocalHitBlue
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LocalHitBlue.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }

                    // Recent queries
                    if (recentQueries.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Недавние запросы", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                TextButton(onClick = { viewModel.clearRecentQueries() }) {
                                    Text("Очистить", color = TextHint, fontSize = 12.sp)
                                }
                            }
                        }
                        items(recentQueries) { q ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.onQueryChanged(q) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.History, null, tint = TextHint, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(q, color = TextPrimary, fontSize = 15.sp)
                            }
                        }
                    }
                }

                // Results: DTC
                if (results.dtcResults.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("DTC-каталог", color = LocalHitBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(results.dtcResults, key = { it.id }) { dtc ->
                        DtcSearchResultCard(dtc = dtc, onClick = { viewModel.selectDtc(dtc) })
                    }
                }

                // Results: Diagnoses
                if (results.diagnosisResults.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("База решений", color = AmberPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(results.diagnosisResults, key = { it.cloudId ?: it.id }) { entity ->
                        DiagnosisSearchResultCard(entity = entity, onClick = { onDiagnosisClick(entity) })
                    }
                }

                // Empty results
                if (!isSearching && query.isNotBlank() && results.dtcResults.isEmpty() && results.diagnosisResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", fontSize = 40.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Ничего не найдено", color = TextSecondary, fontSize = 16.sp)
                                Text("Попробуйте другой запрос", color = TextHint, fontSize = 13.sp)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
