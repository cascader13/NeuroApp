package com.neuroproject.neuro.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.components.LineChart
import com.neuroproject.neuro.data.session.SessionEntity
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    onBackClick: () -> Unit,
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    ChartsScreenContent(
        sessions = sessions,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartsScreenContent(
    sessions: List<SessionEntity>,
    onBackClick: () -> Unit
) {
    val metricsList = listOf(
        "Общий индекс" to { s: SessionEntity -> s.totalIndex?.toFloat() },
        "Субъективный когнитивный" to { s: SessionEntity -> s.subjectiveCognitive?.toFloat() },
        "Субъективный психологический" to { s: SessionEntity -> s.subjectivePsychological?.toFloat() },
        "Субъективный физический" to { s: SessionEntity -> s.subjectivePhysiological?.toFloat() },
        "Объективный когнитивный" to { s: SessionEntity -> s.objectiveCognitive?.toFloat() },
        "Объективный психологический" to { s: SessionEntity -> s.objectivePsychological?.toFloat() },
        "Объективный физический" to { s: SessionEntity -> s.objectivePhysiological?.toFloat() },
        "Общий когнитивный" to { s: SessionEntity -> s.totalCognitive?.toFloat() },
        "Общий психологический" to { s: SessionEntity -> s.totalPsychological?.toFloat() },
        "Общий физический" to { s: SessionEntity -> s.totalPhysiological?.toFloat() }
    )

    val selectedMetrics = remember { mutableStateMapOf<String, Boolean>().apply {
        metricsList.forEach { (name, _) -> put(name, name == "Общий индекс") }
    } }

    val selectedLines = metricsList.filter { (name, _) -> selectedMetrics[name] == true }
            .mapNotNull { (_, extractor) ->
                sessions.map { extractor(it) }
                    .takeIf { it.any { value -> value != null } }
    }

    val xLabels = sessions.map { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it.sessionId)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Графики тенденций") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Выберите метрики:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(metricsList) { (name, _) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                        Checkbox(
                            checked = selectedMetrics[name] ?: false,
                            onCheckedChange = { selectedMetrics[name] = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedLines.isNotEmpty()) {
                LineChart(
                    data = selectedLines,
                    labels = xLabels,
                    modifier = Modifier.fillMaxWidth().height(400.dp)
                )
            } else {
                Text("Выберите хотя бы одну метрику", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChartsScreen() {
    NeuroApplicationTheme {
        val mockSessions = listOf(
            createMockSession(1, 78, 75, 80, 70),
            createMockSession(2, 65, 60, 70, 65),
            createMockSession(3, 82, 85, 80, 78)
        )
        ChartsScreenContent(
            sessions = mockSessions,
            onBackClick = {}
        )
    }
}