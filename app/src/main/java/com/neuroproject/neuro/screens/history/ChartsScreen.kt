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
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    onBackClick: () -> Unit,
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    ChartsScreenContent(
        state = state,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartsScreenContent(
    state: ChartsState,
    onBackClick: () -> Unit
) {
    val sessions = state.sessions
    
    val metricsList = listOf(
        "Общий индекс" to { s: Session -> s.totalIndex?.toFloat() },
        "Субъективный когнитивный" to { s: Session -> s.subjectiveCognitive?.toFloat() },
        "Субъективный психологический" to { s: Session -> s.subjectivePsychological?.toFloat() },
        "Субъективный физический" to { s: Session -> s.subjectivePhysiological?.toFloat() },
        "Объективный когнитивный" to { s: Session -> s.objectiveCognitive?.toFloat() },
        "Объективный психологический" to { s: Session -> s.objectivePsychological?.toFloat() },
        "Объективный физический" to { s: Session -> s.objectivePhysiological?.toFloat() },
        "Общий когнитивный" to { s: Session -> s.totalCognitive?.toFloat() },
        "Общий психологический" to { s: Session -> s.totalPsychological?.toFloat() },
        "Общий физический" to { s: Session -> s.totalPhysiological?.toFloat() }
    )

    val selectedMetrics = remember { mutableStateMapOf<String, Boolean>().apply {
        metricsList.forEach { (name, _) -> put(name, name == "Общий индекс") }
    } }

    val selectedLines = metricsList.filter { (name, _) -> selectedMetrics[name] == true }
            .mapNotNull { (_, extractor) ->
                sessions.map { extractor(it) }
                    .takeIf { it.any { value -> value != null } }
    }

    val xLabels = sessions.map { it.formattedDate }

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