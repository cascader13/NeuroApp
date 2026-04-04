package com.neuroproject.neuro.screens.sensorchecking

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.unit.dp
import com.neuroproject.neuro.R
import com.neuroproject.neuro.components.BackButton
import com.neuroproject.neuro.components.BackHandler
import com.neuroproject.neuro.services.DeviceConnectionState

@Composable
fun SensorCheckingScreen(
    modifier: Modifier = Modifier,
    vm: SensorCheckingScreenViewModel,
    onBackPressed: () -> Unit = {},
    onSensorOk: () -> Unit = {},
    onDeviceUnconnected: () -> Unit = {}
) {
    val connectionState by vm.capsuleDM.connectionState.collectAsState()
    val resistState by vm.resistState.collectAsState()

    // Обработка отключения устройства
    LaunchedEffect(connectionState) {
        when (connectionState) {
            DeviceConnectionState.disconnected,
            DeviceConnectionState.error -> {
                Log.d("SensorCheckingScreen", "Device disconnected or error: $connectionState")
                vm.finish()
                onDeviceUnconnected()
            }

            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        vm.start()
    }

    BackHandler {
        vm.finish()
        onBackPressed()
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (resistState.isAllOk()) 1f else 0f,
        label = "alpha"
    )

    val o1Color = getResistColor(resistState.o1)
    val o2Color = getResistColor(resistState.o2)
    val t3Color = getResistColor(resistState.t3)
    val t4Color = getResistColor(resistState.t4)

    Surface(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
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
                        .matchParentSize()
                        .rotate(180f)
                )

                // Индикаторы качества (рисуются поверх)
                Canvas(modifier = Modifier.matchParentSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Позиции электродов (примерные координаты)
                    val positions = listOf(
                        Offset(canvasWidth / 5f, canvasHeight / 2f) to t3Color,          // T3
                        Offset(canvasWidth / 5f * 4, canvasHeight / 2f) to t4Color,      // T4
                        Offset(canvasWidth / 4f, canvasHeight / 4f * 3) to o1Color,      // O1
                        Offset(canvasWidth / 4f * 3, canvasHeight / 4f * 3) to o2Color   // O2
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

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка "Далее" появляется, когда все датчики OK
            OutlinedButton(
                onClick = {
                    vm.finish()
                    onSensorOk()
                },
                enabled = resistState.isAllOk(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .graphicsLayer { alpha = animatedAlpha },
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            ) {
                Text(
                    text = "Далее",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun getResistColor(state: ResistState): Color {
    return when (state) {
        ResistState.BAD -> Color.Red
        ResistState.OK -> Color.Green
    }
}
