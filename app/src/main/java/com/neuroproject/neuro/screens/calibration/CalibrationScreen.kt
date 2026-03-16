package com.neuroproject.neuro.screens.calibration

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R
import com.neuroproject.neuro.components.BackButton
import com.neuroproject.neuro.components.BackHandler
import java.util.concurrent.TimeUnit

@Composable
fun CalibrationScreen(
    modifier: Modifier = Modifier,
    vm: CalibrationViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onCalibrationComplete: () -> Unit = {},
    onDeviceUnconnected: () -> Unit = {}
) {
    val state by vm.uiState.collectAsState()
    val calibrationValue by vm.state.collectAsState()

    // Логика калибровки
    LaunchedEffect(calibrationValue) {
        Log.d("Calibration", "$calibrationValue")
        if (calibrationValue.value == 6) {
            vm.cancelCalibration()
        }
        if (calibrationValue.value == 4 || calibrationValue.value == 5) {
            vm.forceStopMetronome()
            onCalibrationComplete()
        }
    }

    LaunchedEffect(state.isCalibrating) {
        if (state.isCalibrating) {
            vm.startCalibration()
        }
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onCalibrationComplete()
        }
    }

    BackHandler {
        if (state.isCalibrating) {
            vm.cancelCalibration()
        }
        onBackPressed()
    }

    // Диалог с предложением использовать предыдущие данные калибровки
    if (state.showPreviousCalibrationDialog) {
        AlertDialog(
            onDismissRequest = {
                vm.performNewCalibration()
            },
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
                    onClick = { vm.usePreviousCalibrationData() },
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
                    onClick = { vm.performNewCalibration() },
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            BackButton {
                if (state.isCalibrating) vm.cancelCalibration()
                onBackPressed()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Калибровка",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Иконка закрытых глаз
            Image(
                painter = painterResource(R.drawable.ic_eyes_closed),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Инструкция
            Text(
                text = "Закройте глаза и\nсфокусируйтесь на звуке",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Круговой секундомер
            CircularCountdownTimer(
                progress = state.progress,
                timeRemaining = state.timeRemaining,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (!state.isCalibrating && !state.isComplete) {
                    Button(
                        onClick = { vm.startCalibration() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Начать калибровку")
                    }
                } else if (state.isCalibrating) {
                    OutlinedButton(
                        onClick = { vm.cancelCalibration() },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Отмена")
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CircularCountdownTimer(
    progress: Float,
    timeRemaining: Long,
    modifier: Modifier = Modifier
) {
    // Получаем цвета из темы до вызова Canvas
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val strokeWidth = canvasSize * 0.1f

            // Фоновая окружность
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(canvasSize, canvasSize)
            )

            // Прогресс
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(canvasSize, canvasSize)
            )
        }

        // Оставшееся время
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.headlineMedium,
            color = textColor
        )
    }
}