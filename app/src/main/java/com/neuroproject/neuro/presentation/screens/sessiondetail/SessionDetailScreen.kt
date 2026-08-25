package com.neuroproject.neuro.presentation.screens.sessiondetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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

/**
 * Экран детальной информации о выбранной сессии.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onBackClick: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {

    val session by viewModel.session.collectAsState()
    val isMarked by viewModel.isMarked.collectAsState()
    SessionDetailContent(
        session = session,
        onBackClick = onBackClick,
        isMarked = isMarked ?: true,
        updateComment = {viewModel.updateComment(it)}
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionDetailContent(
    session: Session?,
    onBackClick: () -> Unit,
    isMarked: Boolean,
    updateComment: (String) -> Unit
) {

    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    var showEditComment by remember { mutableStateOf(false) }

    var tempComment by remember { mutableStateOf("") }

    LaunchedEffect(session?.comment) {
        tempComment = session?.comment ?: ""
    }

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
                                SessionCategory.H24_3 -> "0-3"
                                SessionCategory.H3_6 -> "3-6"
                                SessionCategory.H6_9 -> "6-9"
                                SessionCategory.H9_12 -> "9-12"
                                SessionCategory.H12_15 -> "12-15"
                                SessionCategory.H15_18 -> "15-18"
                                SessionCategory.H18_21 -> "18-21"
                                SessionCategory.H21_24 -> "21-24"
                                SessionCategory.TECHNICAL -> "Техническая"
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
                        Button(
                            onClick = {
                                tempComment = s.comment ?: ""
                                showEditComment = true },
                            enabled = isMarked == false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Редактировать комментарий")
                        }

                        if (showEditComment) {
                            MiniCommentDialog(
                                comment = tempComment,
                                onCommentChange = {
                                    tempComment = it },
                                onFinishClick = {
                                    updateComment(tempComment)
                                    showEditComment = false },
                                onDismiss = {
                                    tempComment = session?.comment ?: ""
                                    showEditComment = false
                                }
                            )
                            }
                        }
                    }
                }
            }
        }
    }



@Composable
fun MiniCommentDialog(
    comment: String,
    onCommentChange: (String) -> Unit,
    onFinishClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val maxLength = 1000
    val isMaxLength = comment.length >= maxLength

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Ваш комментарий",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = comment,
                    onValueChange = {
                        if (it.length <= maxLength) onCommentChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Поделитесь впечатлениями...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${comment.length}/$maxLength",
                        color = if (isMaxLength) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFinishClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Завершить")
            }
        }
    )
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
            category = SessionCategory.H18_21,
            comment = "Тестовый комментарий. Было интересно, но немного устал.",
            subjectiveResult = SubjectiveResult(85, 72, 68, 75),
            objectiveResult = ObjectiveFatigueResult(80, 70, 65, 72, "Среднее", "Низкий"),
            totalResult = TotalFatigueResult(82, 71, 66, 78)
        )
        SessionDetailContent(
            session = mockSession,
            onBackClick = {},
            isMarked = true,
            updateComment = {}
        )
    }
}