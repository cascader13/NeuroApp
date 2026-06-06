// presentation/screens/calibration/CalibrationScreen.kt
package com.neuroproject.neuro.presentation.screens.calibration

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R
import com.neuroproject.neuro.components.BackButton
import com.neuroproject.neuro.components.BackHandler
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import com.neuroproject.neuro.ui.theme.ThemeMode

@Composable
fun CalibrationScreen(
    modifier: Modifier = Modifier,
    vm: CalibrationViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onCalibrationComplete: () -> Unit = {},
    onDeviceUnconnected: () -> Unit = {}
) {
    val uiState by vm.uiState.collectAsState()

    // Обработка завершения калибровки
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onCalibrationComplete()
        }
    }

    // Обработка ошибок
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            // Можно показать Snackbar или просто логировать
            android.util.Log.e("Calibration", error)
        }
    }

    BackHandler {
        if (uiState.isCalibrating) {
            vm.cancelCalibration()
        }
        onBackPressed()
    }

    CalibrationScreenContent(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onStartCalibration = { vm.startCalibration() },
        onCancelCalibration = { vm.cancelCalibration() },
        onUsePreviousCalibration = { vm.usePreviousCalibration() },
        onPerformNewCalibration = { vm.performNewCalibration() },
        onDismissDialog = { vm.dismissDialog() }
    )
}

@Composable
fun CalibrationScreenContent(
    uiState: CalibrationUiState,
    onBackPressed: () -> Unit = {},
    onStartCalibration: () -> Unit = {},
    onCancelCalibration: () -> Unit = {},
    onUsePreviousCalibration: () -> Unit = {},
    onPerformNewCalibration: () -> Unit = {},
    onDismissDialog: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            BackButton { onBackPressed() }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Калибровка",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_eyes_closed),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Закройте глаза и\nсфокусируйтесь на звуке",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularCountdownTimer(
                progress = uiState.progress,
                timeRemaining = uiState.timeRemaining,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when {
                    uiState.isCalibrating -> {
                        OutlinedButton(
                            onClick = onCancelCalibration,
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Отмена")
                        }
                    }
                    !uiState.isComplete -> {
                        Button(
                            onClick = onStartCalibration,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Начать калибровку")
                        }
                    }
                }
            }
        }
    }

    // Диалог с предложением использовать предыдущие данные
    if (uiState.showPreviousCalibrationDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = {
                Text(
                    text = "Использовать предыдущие данные?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Хотите использовать данные о калибровке с прошлых сессий?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onUsePreviousCalibration,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Да")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onPerformNewCalibration,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Нет")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CircularCountdownTimer(
    progress: Float,
    timeRemaining: Long,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val strokeWidth = canvasSize * 0.1f

            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(canvasSize, canvasSize)
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(canvasSize, canvasSize)
            )
        }

        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(timeRemaining)
        val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.headlineMedium,
            color = textColor
        )
    }
}

// ============================================================
// PREVIEWS
// ============================================================

@Preview(name = "Светлая тема - Начало")
@Composable
fun PreviewCalibrationStartLight() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        CalibrationScreenContent(
            uiState = CalibrationUiState(
                isCalibrating = false,
                isComplete = false,
                progress = 0f,
                timeRemaining = 60000L
            )
        )
    }
}

@Preview(name = "Тёмная тема - Начало")
@Composable
fun PreviewCalibrationStartDark() {
    NeuroApplicationTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
        CalibrationScreenContent(
            uiState = CalibrationUiState(
                isCalibrating = false,
                isComplete = false,
                progress = 0f,
                timeRemaining = 60000L
            )
        )
    }
}

@Preview(name = "Светлая тема - Калибровка")
@Composable
fun PreviewCalibrationActiveLight() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        CalibrationScreenContent(
            uiState = CalibrationUiState(
                isCalibrating = true,
                isComplete = false,
                progress = 0.3f,
                timeRemaining = 42000L
            )
        )
    }
}

@Preview(name = "Светлая тема - Завершено")
@Composable
fun PreviewCalibrationCompleteLight() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        CalibrationScreenContent(
            uiState = CalibrationUiState(
                isCalibrating = false,
                isComplete = true,
                progress = 1f,
                timeRemaining = 0L
            )
        )
    }
}

@Preview(name = "Светлая тема - Диалог")
@Composable
fun PreviewCalibrationDialogLight() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        CalibrationScreenContent(
            uiState = CalibrationUiState(
                isCalibrating = false,
                isComplete = false,
                showPreviousCalibrationDialog = true
            )
        )
    }
}