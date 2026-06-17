// presentation/screens/sensorchecking/SensorCheckingScreen.kt
package com.neuroproject.neuro.presentation.screens.sensorchecking

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R
import com.neuroproject.neuro.components.BackHandler
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import com.neuroproject.neuro.ui.theme.ThemeMode

@Composable
fun SensorCheckingScreen(
    modifier: Modifier = Modifier,
    vm: SensorCheckingViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onSensorOk: () -> Unit = {},
    onDeviceUnconnected: () -> Unit = {}
) {
    val uiState by vm.uiState.collectAsState()
    val isDeviceDisconnected by vm.isDeviceDisconnected.collectAsState()

    LaunchedEffect(Unit) {
        vm.start()
    }

    LaunchedEffect(isDeviceDisconnected) {
        if (isDeviceDisconnected) {
            vm.finish()
        }
    }

    if (isDeviceDisconnected) {
        AlertDialog(
            onDismissRequest = { onDeviceUnconnected() },
            title = {
                Text(
                    text = "Устройство отключено",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Связь с устройством потеряна. Возврат в главное меню...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDeviceUnconnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }

    BackHandler {
        vm.finish()
        onBackPressed()
    }

    SensorCheckingScreenContent(
        uiState = uiState,
        onFinish = {
            vm.finish()
            onSensorOk()
        },
        onBackPressed = onBackPressed
    )
}

@Composable
fun SensorCheckingScreenContent(
    uiState: SensorCheckingUiState,
    onFinish: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (uiState.electrodeStates.isAllOk()) 1f else 0f,
        label = "alpha"
    )

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
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Качество\nналожения",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Контейнер с изображением и индикаторами
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            ) {
                // Изображение оголовья
                Image(
                    painter = painterResource(R.drawable.headband),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()  // ✅ вместо matchParentSize()
                        .rotate(180f)
                )

                // Индикаторы качества (рисуются поверх)
                ElectrodeIndicators(
                    o1State = uiState.electrodeStates.o1,
                    o2State = uiState.electrodeStates.o2,
                    t3State = uiState.electrodeStates.t3,
                    t4State = uiState.electrodeStates.t4
                )

                // Индикатор заряда батареи
                BatteryIndicator(batteryCharge = uiState.batteryCharge)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка "Далее" появляется, когда все датчики OK
            Button(
                onClick = onFinish,
                enabled = uiState.electrodeStates.isAllOk(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .graphicsLayer { alpha = animatedAlpha },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Далее")
            }
        }
    }
}

@Composable
fun ElectrodeIndicators(
    o1State: ElectrodeState,
    o2State: ElectrodeState,
    t3State: ElectrodeState,
    t4State: ElectrodeState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {  // ✅ fillMaxSize вместо matchParentSize
        val canvasWidth = size.width
        val canvasHeight = size.height

        fun getColor(state: ElectrodeState): Color = when (state) {
            ElectrodeState.OK -> Color.Green
            ElectrodeState.BAD -> Color.Red
        }

        // Позиции электродов (примерные координаты)
        val positions = listOf(
            Offset(canvasWidth / 5f, canvasHeight / 2f) to getColor(t3State),          // T3
            Offset(canvasWidth / 5f * 4, canvasHeight / 2f) to getColor(t4State),      // T4
            Offset(canvasWidth / 4f, canvasHeight / 4f * 3) to getColor(o1State),      // O1
            Offset(canvasWidth / 4f * 3, canvasHeight / 4f * 3) to getColor(o2State)   // O2
        )

        positions.forEach { (center, color) ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    radius = canvasWidth * 0.1f,
                    center = center
                ),
                radius = canvasWidth * 0.1f,
                center = center
            )
        }
    }
}

@Composable
fun BatteryIndicator(batteryCharge: Float) {
    // ✅ Используем Box и Modifier.align внутри BoxScope
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)  // ✅ align работает внутри Box
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔋 ${batteryCharge.toInt()}%",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

// ============================================================
// PREVIEWS
// ============================================================

@Preview(name = "Светлая тема - Плохой контакт")
@Composable
fun PreviewSensorCheckingBadLight() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        SensorCheckingScreenContent(
            uiState = SensorCheckingUiState(
                electrodeStates = ElectrodeStates(
                    o1 = ElectrodeState.BAD,
                    o2 = ElectrodeState.BAD,
                    t3 = ElectrodeState.BAD,
                    t4 = ElectrodeState.BAD
                ),
                batteryCharge = 75f
            )
        )
    }
}

@Preview(name = "Светлая тема - Хороший контакт")
@Composable
fun PreviewSensorCheckingOkLight() {
    NeuroApplicationTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
        SensorCheckingScreenContent(
            uiState = SensorCheckingUiState(
                electrodeStates = ElectrodeStates(
                    o1 = ElectrodeState.OK,
                    o2 = ElectrodeState.OK,
                    t3 = ElectrodeState.OK,
                    t4 = ElectrodeState.OK
                ),
                batteryCharge = 75f
            )
        )
    }
}

@Preview(name = "Тёмная тема - Плохой контакт")
@Composable
fun PreviewSensorCheckingBadDark() {
    NeuroApplicationTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
        SensorCheckingScreenContent(
            uiState = SensorCheckingUiState(
                electrodeStates = ElectrodeStates(
                    o1 = ElectrodeState.BAD,
                    o2 = ElectrodeState.BAD,
                    t3 = ElectrodeState.BAD,
                    t4 = ElectrodeState.BAD
                ),
                batteryCharge = 30f
            )
        )
    }
}