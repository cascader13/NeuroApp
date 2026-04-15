package com.neuroproject.neuro.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onChartClick: () -> Unit,
    onSessionClick: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    HistoryScreenContent(
        state = state,
        onBackClick = onBackClick,
        onChartClick = onChartClick,
        onSessionClick = onSessionClick,
        onRetry = { viewModel.loadSessions() },
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreenContent(
    state: HistoryState,
    onBackClick: () -> Unit,
    onChartClick: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История сессий") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onChartClick) {
                        Icon(Icons.Default.Timeline, "Графики")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    ErrorScreen(
                        error = state.error!!,
                        onRetry = onRetry,
                        onClearError = onClearError
                    )
                }
                state.sessions.isEmpty() -> {
                    EmptyScreen()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.sessions) { session ->
                            SessionCard(
                                session = session,
                                onClick = { onSessionClick(session.sessionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ошибка загрузки",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, "Повторить")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Повторить")
        }
    }
}

@Composable
private fun EmptyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет сессий",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "История сессий пуста.\nНачните первую сессию для записи данных.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SessionCard(
    session: Session,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${session.formattedDate} ${session.formattedTime}",
                    style = MaterialTheme.typography.titleMedium
                )
                session.totalIndex?.let {
                    Text("Общий: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Когн: ${session.totalCognitive ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Псих: ${session.totalPsychological ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Физ: ${session.totalPhysiological ?: "—"}", style = MaterialTheme.typography.bodySmall)
            }
            session.comment?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

fun createMockSession(offsetDays: Int, totalIndex: Int, totalCog: Int, totalPsy: Int, totalPhys: Int): Session {
    val now = System.currentTimeMillis()
    return Session(
        sessionId = now - offsetDays * 86400000L,
        startTime = now - offsetDays * 86400000L,
        totalIndex = totalIndex,
        totalCognitive = totalCog,
        totalPsychological = totalPsy,
        totalPhysiological = totalPhys,
        subjectiveCognitive = totalCog,
        subjectivePsychological = totalPsy,
        subjectivePhysiological = totalPhys,
        objectiveCognitive = totalCog,
        objectivePsychological = totalPsy,
        objectivePhysiological = totalPhys,
        expeditionId = "0",
        userId = "0"
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewHistoryScreen() {
    NeuroApplicationTheme {
        val mockSessions = listOf(
            createMockSession(1, 78, 75, 80, 70),
            createMockSession(2, 65, 60, 70, 65),
            createMockSession(3, 82, 85, 80, 78)
        )
        HistoryScreenContent(
            state = HistoryState(sessions = mockSessions),
            onBackClick = {},
            onChartClick = {},
            onSessionClick = {},
            onRetry = {},
            onClearError = {}
        )
    }
}