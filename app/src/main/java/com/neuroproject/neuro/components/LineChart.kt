package com.neuroproject.neuro.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Минимальный Compose-график для экрана ChartsScreen.
 *
 * Компонент не зависит от ViewModel/Room и принимает только подготовленные серии данных.
 */
@Composable
fun LineChart(
    data: List<List<Float?>>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )

    Box(modifier = modifier.padding(8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val values = data.flatten().filterNotNull()
            if (values.isEmpty()) return@Canvas

            val minValue = values.minOrNull() ?: 0f
            val maxValue = values.maxOrNull() ?: 100f
            val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
            val left = 24.dp.toPx()
            val right = size.width - 8.dp.toPx()
            val top = 8.dp.toPx()
            val bottom = size.height - 24.dp.toPx()
            val chartWidth = (right - left).coerceAtLeast(1f)
            val chartHeight = (bottom - top).coerceAtLeast(1f)
            val pointCount = data.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1

            drawLine(Color.Gray, Offset(left, top), Offset(left, bottom), strokeWidth = 2f)
            drawLine(Color.Gray, Offset(left, bottom), Offset(right, bottom), strokeWidth = 2f)

            data.forEachIndexed { seriesIndex, series ->
                val color = palette[seriesIndex % palette.size]
                val points = series.mapIndexedNotNull { index, value ->
                    value?.let {
                        val x = left + if (pointCount == 1) 0f else chartWidth * index / (pointCount - 1)
                        val y = bottom - ((it - minValue) / range) * chartHeight
                        Offset(x, y)
                    }
                }
                points.zipWithNext().forEach { (start, end) ->
                    drawLine(color, start, end, strokeWidth = 4f)
                }
                points.forEach { point -> drawCircle(color, radius = 5f, center = point) }
            }
        }
    }
}
