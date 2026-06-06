package com.neuroproject.neuro.presentation.screens.sessiondetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SubjectiveResult
import com.neuroproject.neuro.domain.model.ObjectiveFatigueResult
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onBackClick: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    SessionDetailContent(
        session = session,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionDetailContent(
    session: Session?,
    onBackClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали сессии") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (session == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                session?.let { s ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Дата и общий индекс
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = dateFormat.format(Date(s.sessionId)),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Общий индекс: ${s.totalIndex ?: "—"}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        // Субъективные показатели
                        InfoCard("Субъективные показатели") {
                            DetailRow("Когнитивный", s.subjectiveCognitive)
                            DetailRow("Психологический", s.subjectivePsychological)
                            DetailRow("Физический", s.subjectivePhysiological)
                        }

                        // Объективные показатели
                        InfoCard("Объективные показатели") {
                            DetailRow("Когнитивный", s.objectiveCognitive)
                            DetailRow("Психологический", s.objectivePsychological)
                            DetailRow("Физический", s.objectivePhysiological)
                        }

                        // Интегральные показатели
                        InfoCard("Интегральные показатели") {
                            DetailRow("Когнитивный", s.totalCognitive)
                            DetailRow("Психологический", s.totalPsychological)
                            DetailRow("Физический", s.totalPhysiological)
                        }

                        // Средние значения
                        InfoCard("Средние") {
                            DetailRow("Субъективный средний", s.averageSubjective)
                            DetailRow("Объективный средний", s.averageObjective)
                        }

                        // Метаданные (без комментария)
                        InfoCard("Метаданные") {
                            DetailRow("Длительность (мин)", s.durationMinutes)
                            DetailRow("Категория", s.category?.let { when(it) {
                                SessionCategory.MORNING -> "Утро"
                                SessionCategory.DAY -> "День"
                                SessionCategory.EVENING -> "Вечер"
                                SessionCategory.TECHNICAL -> "Технический"
                            } } ?: "—")
                        }

                        // Комментарий (отдельная карточка)
                        if (!s.comment.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Комментарий", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = s.comment,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: Any?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value?.toString() ?: "—", style = MaterialTheme.typography.bodyMedium)
    }
}

// ==================== ПРЕДПРОСМОТР ====================

@Preview(showBackground = true)
@Composable
fun PreviewSessionDetailScreen() {
    NeuroApplicationTheme {
        val mockSession = Session(
            sessionId = System.currentTimeMillis(),
            userId = "0",
            expeditionId = "0",
            durationMinutes = 10,
            category = SessionCategory.EVENING,
            comment = "Тестовый комментарий. Было интересно, но немного устал.",
            subjectiveResult = SubjectiveResult(85, 72, 68, 75),
            objectiveResult = ObjectiveFatigueResult(80, 70, 65, 72, "Среднее", "Низкий"),
            totalResult = TotalFatigueResult(82, 71, 66, 78)
        )
        SessionDetailContent(
            session = mockSession,
            onBackClick = {}
        )
    }
}