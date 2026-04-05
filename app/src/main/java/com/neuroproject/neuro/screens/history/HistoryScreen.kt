package com.neuroproject.neuro.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.data.session.SessionEntity
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onChartClick: () -> Unit,
    onSessionClick: (Long) -> Unit,  // новый параметр
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    HistoryScreenContent(
        sessions = sessions,
        onBackClick = onBackClick,
        onChartClick = onChartClick,
        onSessionClick = onSessionClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreenContent(
    sessions: List<SessionEntity>,
    onBackClick: () -> Unit,
    onChartClick: () -> Unit,
    onSessionClick: (Long) -> Unit
) {
    val isLoading = sessions.isEmpty()
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sessions) { session ->
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

@Composable
fun SessionCard(
    session: SessionEntity,
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
                Text(dateFormat.format(Date(session.sessionId)), style = MaterialTheme.typography.titleMedium)
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

fun createMockSession(offsetDays: Int, totalIndex: Int, totalCog: Int, totalPsy: Int, totalPhys: Int): SessionEntity {
    val now = System.currentTimeMillis()
    return SessionEntity(
        sessionId = now - offsetDays * 86400000L,
        totalIndex = totalIndex,
        totalCognitive = totalCog,
        totalPsychological = totalPsy,
        totalPhysiological = totalPhys,
        subjectiveCognitive = totalCog,
        subjectivePsychological = totalPsy,
        subjectivePhysiological = totalPhys,
        objectiveCognitive = totalCog,
        objectivePsychological = totalPsy,
        objectivePhysiological = totalPhys
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
            sessions = mockSessions,
            onBackClick = {},
            onChartClick = {},
            onSessionClick = {}
        )
    }
}