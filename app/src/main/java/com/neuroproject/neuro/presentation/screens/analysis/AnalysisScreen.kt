package com.neuroproject.neuro.presentation.screens.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.NFBSample
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    vm: AnalysisScreenViewModel = hiltViewModel(),
    onDeviceUnconnected: () -> Unit = {}
) {
    val plotData by vm.plotData.collectAsState()
    val nfbData by vm.nfb.collectAsState()
    val isRecording by vm.isRecording.collectAsState()
    val connectionState by vm.connectionState.collectAsState()

    LaunchedEffect(connectionState) {
        if (connectionState == DeviceConnectionState.disconnected || connectionState == DeviceConnectionState.error) {
            onDeviceUnconnected()
        }
    }

    AnalysisScreenContent(
        modifier = modifier,
        nfbData = nfbData,
        plotData = plotData,
        isRecording = isRecording,
        connectionState = connectionState,
        onBackPressed = onBackPressed,
        onClear = vm::clearPlotData,
        onStartRecording = vm::startRecording,
        onStopRecording = vm::stopRecording
    )
}

@Composable
fun AnalysisScreenContent(
    modifier: Modifier = Modifier,
    nfbData: NFBSample,
    plotData: PlotData,
    isRecording: Boolean,
    connectionState: DeviceConnectionState,
    onBackPressed: () -> Unit,
    onClear: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("analysis_screen")
    ) {
        AnalysisHeader(
            nfbData = nfbData,
            isRecording = isRecording,
            connectionState = connectionState,
            onBackPressed = onBackPressed,
            onClear = onClear,
            onStartRecording = onStartRecording,
            onStopRecording = onStopRecording
        )

        NeuroGraphs(
            plotData = plotData,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )

        LegendSection()
    }
}

@Composable
private fun AnalysisHeader(
    nfbData: NFBSample,
    isRecording: Boolean,
    connectionState: DeviceConnectionState,
    onBackPressed: () -> Unit,
    onClear: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("analysis_header")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackPressed, modifier = Modifier.testTag("analysis_back")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
                Column {
                    Text(
                        text = if (isRecording) "Мониторинг • Запись" else "Мониторинг",
                        color = if (isRecording) Color.Red else Color.White,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = connectionState.name,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("analysis_connection_state")
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (isRecording) onStopRecording() else onStartRecording() },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("analysis_record_toggle"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color.Red else Color.Green
                    )
                ) {
                    Text(if (isRecording) "Стоп" else "Запись", fontSize = 12.sp)
                }

                Button(
                    onClick = onClear,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("analysis_clear")
                ) {
                    Text("Очистить", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ValueDisplay(label = "Alpha", value = nfbData.alpha, color = Color(0xFF4FC3F7))
            ValueDisplay(label = "Beta", value = nfbData.beta, color = Color(0xFFF44336))
            ValueDisplay(label = "Delta", value = nfbData.delta, color = Color(0xFF66BB6A))
        }
    }
}

@Composable
private fun ValueDisplay(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.testTag("analysis_value_$label")) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(
            text = "%.2f".format(value),
            color = color,
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun NeuroGraphs(plotData: PlotData, modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("analysis_graphs")) {
        NeuroGraph(
            points = plotData.alphaPoints,
            color = Color(0xFF4FC3F7),
            label = "Alpha Waves",
            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
        )
        NeuroGraph(
            points = plotData.betaPoints,
            color = Color(0xFFF44336),
            label = "Beta Waves",
            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
        )
        NeuroGraph(
            points = plotData.deltaPoints,
            color = Color(0xFF66BB6A),
            label = "Delta Waves",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NeuroGraph(
    points: List<DataPoint>,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.testTag("analysis_graph_$label")) {
        Text(text = label, color = color, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
        ) {
            if (points.size < 2) {
                drawPlaceholder()
                return@Canvas
            }
            val padding = 24.dp.toPx()
            val graphWidth = size.width - 2 * padding
            val graphHeight = size.height - 2 * padding
            val bounds = calculateBounds(points)
            drawGrid(padding)
            drawWaveLine(points, color, padding, graphWidth, graphHeight, bounds)
        }
    }
}

private fun DrawScope.drawPlaceholder() {
    drawCircle(
        color = Color.White.copy(alpha = 0.1f),
        center = Offset(size.width / 2, size.height / 2),
        radius = 20.dp.toPx()
    )
}

private fun calculateBounds(points: List<DataPoint>): WaveBounds {
    val minX = points.minOfOrNull { it.x } ?: 0f
    val maxX = points.maxOfOrNull { it.x } ?: 1f
    val maxY = ((points.maxOfOrNull { it.y } ?: 1f) * 1.1f).coerceAtLeast(1f)
    return WaveBounds(minX = minX, maxX = maxX, minY = 0f, maxY = maxY)
}

private data class WaveBounds(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float
) {
    val xRange: Float get() = (maxX - minX).takeIf { it > 0f } ?: 1f
    val yRange: Float get() = (maxY - minY).takeIf { it > 0f } ?: 1f
}

private fun DrawScope.drawGrid(padding: Float) {
    drawLine(
        start = Offset(padding, size.height - padding),
        end = Offset(size.width - padding, size.height - padding),
        color = Color.White.copy(alpha = 0.3f),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        start = Offset(padding, padding),
        end = Offset(padding, size.height - padding),
        color = Color.White.copy(alpha = 0.3f),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawWaveLine(
    points: List<DataPoint>,
    color: Color,
    padding: Float,
    graphWidth: Float,
    graphHeight: Float,
    bounds: WaveBounds
) {
    val path = Path().apply {
        points.forEachIndexed { index, point ->
            val x = padding + ((point.x - bounds.minX) / bounds.xRange) * graphWidth
            val y = size.height - padding - ((point.y - bounds.minY) / bounds.yRange) * graphHeight
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
    }

    drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

    points.takeLast(10).forEach { point ->
        val x = padding + ((point.x - bounds.minX) / bounds.xRange) * graphWidth
        val y = size.height - padding - ((point.y - bounds.minY) / bounds.yRange) * graphHeight
        drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(x, y))
    }
}

@Composable
private fun LegendSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("analysis_legend"),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        LegendItem(color = Color(0xFF4FC3F7), text = "Alpha")
        LegendItem(color = Color(0xFFF44336), text = "Beta")
        LegendItem(color = Color(0xFF66BB6A), text = "Delta")
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAnalysisScreenContent() {
    NeuroApplicationTheme {
        AnalysisScreenContent(
            nfbData = NFBSample(alpha = 1.5f, beta = 2.5f, delta = 0.8f),
            plotData = PlotData(
                alphaPoints = listOf(DataPoint(0f, 1f), DataPoint(1f, 2f)),
                betaPoints = listOf(DataPoint(0f, 2f), DataPoint(1f, 1f)),
                deltaPoints = listOf(DataPoint(0f, 0.5f), DataPoint(1f, 0.8f))
            ),
            isRecording = false,
            connectionState = DeviceConnectionState.connected,
            onBackPressed = {},
            onClear = {},
            onStartRecording = {},
            onStopRecording = {}
        )
    }
}
