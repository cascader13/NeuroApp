// presentation/screens/history/HistoryScreen.kt
package com.neuroproject.neuro.presentation.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.domain.model.HistoryState
import com.neuroproject.neuro.domain.model.Session
import com.neuroproject.neuro.domain.model.SessionCategory
import com.neuroproject.neuro.domain.model.TotalFatigueResult
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import kotlinx.coroutines.launch

/**
 * Экран истории пройденных сессий с возможностью просмотра и удаления.
 */
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onChartClick: () -> Unit,
    onSessionClick: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Показываем снекбар при успешном удалении
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Сессия успешно удалена")
                viewModel.clearDeleteSuccess()
            }
        }
    }

    HistoryScreenContent(
        state = state,
        onBackClick = onBackClick,
        onChartClick = onChartClick,
        onSessionClick = onSessionClick,
        onRetry = viewModel::loadSessions,
        onClearError = viewModel::clearError,
        onDeleteSession = viewModel::deleteSession,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenContent(
    state: HistoryState,
    onBackClick: () -> Unit,
    onChartClick: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = Modifier.testTag("history_screen"),
        topBar = {
            TopAppBar(
                title = { Text("История сессий") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("history_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onChartClick, modifier = Modifier.testTag("history_charts")) {
                        Icon(Icons.Default.Timeline, contentDescription = "Графики")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("history_loading")
                )

                state.error != null -> HistoryErrorContent(
                    error = state.error,
                    onRetry = onRetry,
                    onClearError = onClearError
                )

                state.sessions.isEmpty() -> HistoryEmptyContent()

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("history_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.sessions,
                        key = { it.sessionId }
                    ) { session ->
                        SessionCard(
                            session = session,
                            onClick = { onSessionClick(session.sessionId) },
                            onDelete = { onDeleteSession(session.sessionId) },
                            isDeleting = state.isDeleting
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryErrorContent(
    error: String?,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("history_error"),
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
        Text("Ошибка загрузки", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error ?: "Неизвестная ошибка",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRetry, modifier = Modifier.testTag("history_retry")) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Повторить")
            }
            Button(onClick = onClearError, modifier = Modifier.testTag("history_clear_error")) {
                Text("Скрыть")
            }
        }
    }
}

@Composable
private fun HistoryEmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("history_empty"),
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
    onClick: () -> Unit,
    onDelete: () -> Unit,
    isDeleting: Boolean = false
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить сессию") },
            text = { Text("Вы уверены, что хотите удалить сессию от ${session.formattedDate} ${session.formattedTime}? Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDeleting, onClick = onClick)
            .testTag("history_session_${session.sessionId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Верхняя строка: дата, время, категория и ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${session.formattedDate} ${session.formattedTime}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = session.category.getDisplayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Общий: ${session.totalIndex ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !isDeleting,
                        modifier = Modifier.testTag("delete_session_${session.sessionId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация об участнике и экспедиции (DEBUG DATA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Участник: ${session.userId.takeIf { it?.isNotBlank() == true } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Экспедиция: ${session.expeditionId.takeIf { it?.isNotBlank() == true } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Индексы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Когн: ${session.totalCognitive ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Псих: ${session.totalPsychological ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Физ: ${session.totalPhysiological ?: "—"}", style = MaterialTheme.typography.bodySmall)
            }

            session.comment?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }

            if (session.passedPrematurely) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Завершена досрочно",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Добавляем разделитель для отладки
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Session ID: ${session.sessionId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private fun createMockSession(offsetDays: Int, totalIndex: Int, totalCog: Int, totalPsy: Int, totalPhys: Int): Session {
    val now = System.currentTimeMillis()
    return Session(
        sessionId = now - offsetDays * 86_400_000L,
        userId = "test_user",
        startTime = now - offsetDays * 86_400_000L,
        durationMinutes = 15,
        category = SessionCategory.H6_9,
        totalResult = TotalFatigueResult(
            cognitiveIndex = totalCog,
            psychologicalIndex = totalPsy,
            physiologicalIndex = totalPhys,
            averageIndex = totalIndex
        ),
        comment = "Тестовая сессия",
        passedPrematurely = false
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewHistoryScreen() {
    NeuroApplicationTheme {
        HistoryScreenContent(
            state = HistoryState(
                sessions = listOf(
                    createMockSession(1, 78, 75, 80, 70),
                    createMockSession(2, 65, 60, 70, 65),
                    createMockSession(3, 82, 85, 80, 78)
                )
            ),
            onBackClick = {},
            onChartClick = {},
            onSessionClick = {},
            onRetry = {},
            onClearError = {},
            onDeleteSession = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHistoryEmpty() {
    NeuroApplicationTheme {
        HistoryScreenContent(
            state = HistoryState(sessions = emptyList()),
            onBackClick = {},
            onChartClick = {},
            onSessionClick = {},
            onRetry = {},
            onClearError = {},
            onDeleteSession = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHistoryError() {
    NeuroApplicationTheme {
        HistoryScreenContent(
            state = HistoryState(error = "Ошибка загрузки данных"),
            onBackClick = {},
            onChartClick = {},
            onSessionClick = {},
            onRetry = {},
            onClearError = {},
            onDeleteSession = {}
        )
    }
}


fun SessionCategory.getDisplayName(): String = when (this) {
    SessionCategory.H24_3 -> "0-3"
    SessionCategory.H3_6 -> "3-6"
    SessionCategory.H6_9 -> "6-9"
    SessionCategory.H9_12 -> "9-12"
    SessionCategory.H12_15 -> "12-15"
    SessionCategory.H15_18 -> "15-18"
    SessionCategory.H18_21 -> "18-21"
    SessionCategory.H21_24 -> "21-24"
    SessionCategory.TECHNICAL -> "Техническая"
}