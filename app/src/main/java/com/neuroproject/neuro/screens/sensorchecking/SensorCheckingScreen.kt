package com.neuroproject.neuro.screens.sensorchecking

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.neuroproject.neuro.components.BackButton
import com.neuroproject.neuro.components.BackHandler
import com.neuroproject.neuro.services.DeviceConnectionState
import com.neuroproject.neuro.ui.theme.Bad
import com.neuroproject.neuro.ui.theme.Ok

//НАДО ПОЛНОСТЬЮ ПЕРЕДЕЛАТЬ. Да, эта часть работает, безусловно. Но это не наш код
@Composable
fun SensorCheckingScreen(
    modifier: Modifier = Modifier,
    vm: SensorCheckingScreenViewModel,
    onBackPressed: () -> Unit = {},
    onSensorOk: () -> Unit = {},
    onDeviceUnconnected: () -> Unit = {}
) {
    val connectionState by vm.capsuleDM.connectionState.collectAsState()
    Surface(
        color = Color.Black, modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Red)
    ) {

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

        LaunchedEffect(vm.capsuleDM.getConnectionState()) {

        }
        DisposableEffect(Unit) {
            onDispose {

            }
        }
        val state = vm.resistState.collectAsState()
        val animatedAlpha by animateFloatAsState(
            targetValue = if (state.value.isAllOk()) 1.0f else 0f,
            label = "alpha"
        )
        BackHandler {
            vm.finish()
            onBackPressed()
        }
        Column(modifier = modifier.fillMaxHeight()
            .background(Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF272727)))),) {
            BackButton { onBackPressed() }
            Spacer(Modifier.height(24.dp))
            Text(
                "Качество\nналожения",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 36.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.aspectRatio(1f)
            ){
                Image(
                    modifier = Modifier.aspectRatio(1f).rotate(180f),
                    painter = painterResource(R.drawable.headband),
                    contentDescription = "headband"
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val o1Color = getResistColor(state.value.o1)
                    val o2Color = getResistColor(state.value.o2)
                    val t3Color = getResistColor(state.value.t3)
                    val t4Color = getResistColor(state.value.t4)

                    drawCircle(Brush.radialGradient(listOf(t3Color, Color.Transparent),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 5f, canvasHeight / 2f)),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 5f, canvasHeight / 2f))
                    drawCircle(Brush.radialGradient(colors = listOf(t4Color, Color.Transparent),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 5f * 4, canvasHeight / 2f)),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 5f * 4, canvasHeight / 2f))
                    drawCircle(Brush.radialGradient(colors = listOf(o1Color, Color.Transparent),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 4f, canvasHeight / 4f * 3)),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 4f, canvasHeight / 4f * 3))
                    drawCircle(Brush.radialGradient(colors = listOf(o2Color, Color.Transparent),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 4f * 3, canvasHeight / 4f * 3)),
                        radius = canvasWidth * 0.1f,
                        center = Offset(canvasWidth / 4f * 3, canvasHeight / 4f * 3))
                }
            }

            Spacer(Modifier.weight(1f))
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .padding(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().graphicsLayer {
                        alpha = animatedAlpha
                    },
                    border = BorderStroke(2.dp, Color.White),
                    enabled = state.value.isAllOk(),
                    onClick = {
                        vm.finish()
                        onSensorOk()
                    }) {
                    Text("Далее", color = Color.White)
                }
            }
        }
    }
}

fun getResistColor(state: ResistState): Color {
    return when(state){
        ResistState.BAD -> Bad
        ResistState.OK -> Ok
    }

}

@Preview
@Composable
fun Preview() {
    SensorCheckingScreen(vm = hiltViewModel()) { }
}