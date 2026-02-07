package com.neuroproject.neuro.screens.calibration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onCalibrationComplete: () -> Unit = {}
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(vm.state) {
        Log.d("Calibration", "${vm.state.value.value}")
        if(vm.state.value.value % 2 == 0){
            state.ClosedEyes = true
        }else{
            state.ClosedEyes = false
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

    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF272727)))),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BackButton {
                if (state.isCalibrating) {
                    vm.cancelCalibration()
                }
                onBackPressed()
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Калибровка",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 36.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            // Иконка закрытых глаз
            Image(
                painter = painterResource(R.drawable.ic_eyes_closed),
                contentDescription = "Eyes closed",
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(1f)
            )

            Spacer(Modifier.height(32.dp))

            // Инструкция
            Text(
                "Закройте глаза и\nсфокусируйтесь на звуке",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // Круговой секундомер
            CircularCountdownTimer(
                progress = state.progress,
                timeRemaining = state.timeRemaining,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (!state.isCalibrating && !state.isComplete) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { vm.startCalibration() }
                    ) {
                        Text("Начать калибровку", color = Color.Black)
                    }
                } else if (state.isCalibrating) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(2.dp, Color.Red),
                        onClick = { vm.cancelCalibration() }
                    ) {
                        Text("Отмена", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun CircularCountdownTimer(
    progress: Float,
    timeRemaining: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val strokeWidth = canvasSize * 0.1f

            // Фоновая окружность
            drawArc(
                color = Color(0xFF333333),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(canvasSize, canvasSize)
            )

            // Прогресс
            drawArc(
                color = Color(0xFF4CAF50),
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
            color = Color.White,
            fontSize = 24.sp
        )
    }
}

@Preview
@Composable
fun PreviewCalibrationScreen() {
    CalibrationScreen()
}
