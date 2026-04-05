package com.neuroproject.neuro.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LineChart(
    data: List<List<Float?>>,
    labels: List<String> = emptyList(),
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    ),
    modifier: Modifier = Modifier,
    yRange: Pair<Float, Float>? = null
) {
    if (data.isEmpty()) return
    val allValues = data.flatten().filterNotNull()
    if (allValues.isEmpty()) return

    val minY = yRange?.first ?: allValues.minOrNull() ?: 0f
    val maxY = yRange?.second ?: allValues.maxOrNull() ?: 1f
    val yRangeSpan = if (maxY == minY) 1f else maxY - minY
    val xCount = data.maxOf { it.size }  // максимальное количество точек

    val axisColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
    ) {
        val width = size.width
        val height = size.height
        val leftMargin = 40.dp.toPx()
        val rightMargin = 20.dp.toPx()
        val topMargin = 20.dp.toPx()
        val bottomMargin = 40.dp.toPx()
        val graphWidth = width - leftMargin - rightMargin
        val graphHeight = height - topMargin - bottomMargin

        drawGridAndAxes(
            leftMargin, topMargin, graphWidth, graphHeight,
            minY, maxY, xCount, labels, axisColor, textColor
        )

        data.forEachIndexed { lineIndex, values ->
            val color = colors[lineIndex % colors.size]
            var lastPoint: Offset? = null
            values.forEachIndexed { i, value ->
                if (value != null && value != 0f) {
                    val step = if (xCount > 1) (xCount - 1) else 1
                    val x = leftMargin + (i.toFloat() / step) * graphWidth
                    val y = topMargin + graphHeight - ((value - minY) / yRangeSpan) * graphHeight
                    val point = Offset(x, y)
                    lastPoint?.let { drawLine(color, it, point, strokeWidth = 3.dp.toPx()) }
                    drawCircle(color, radius = 4.dp.toPx(), center = point)
                    lastPoint = point
                } else {
                    lastPoint = null
                }
            }
        }
    }
}

private fun DrawScope.drawGridAndAxes(
    leftMargin: Float,
    topMargin: Float,
    graphWidth: Float,
    graphHeight: Float,
    minY: Float,
    maxY: Float,
    xCount: Int,
    labels: List<String>,
    axisColor: Color,
    textColor: Color
) {
    // Отрисовка вертикальных линий и подписей X с шагом, чтобы не наслаивались
    val maxLabels = 6  // максимум подписей
    val step = if (xCount > maxLabels) (xCount - 1) / maxLabels else 1

    for (i in 0 until xCount) {
        val stepCoord = if (xCount > 1) (xCount - 1) else 1
        val x = leftMargin + (i.toFloat() / stepCoord) * graphWidth
        drawLine(axisColor, start = Offset(x, topMargin), end = Offset(x, topMargin + graphHeight), strokeWidth = 1.dp.toPx())

        // Показываем подпись только если индекс кратен шагу
        if (labels.isNotEmpty() && i < labels.size && i % step == 0) {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 12.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(labels[i], x, topMargin + graphHeight + 20.dp.toPx(), paint)
            }
        }
    }

    // Горизонтальные линии и подписи Y
    val ySteps = 4
    for (i in 0..ySteps) {
        val y = topMargin + (i.toFloat() / ySteps) * graphHeight
        drawLine(axisColor, start = Offset(leftMargin, y), end = Offset(leftMargin + graphWidth, y), strokeWidth = 1.dp.toPx())
        val value = maxY - (i.toFloat() / ySteps) * (maxY - minY)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = textColor.toArgb()
                textSize = 12.dp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText(String.format("%.0f", value), leftMargin - 8.dp.toPx(), y + 4.dp.toPx(), paint)
        }
    }

    // Оси
    drawLine(axisColor, start = Offset(leftMargin, topMargin + graphHeight), end = Offset(leftMargin + graphWidth, topMargin + graphHeight), strokeWidth = 2.dp.toPx())
    drawLine(axisColor, start = Offset(leftMargin, topMargin), end = Offset(leftMargin, topMargin + graphHeight), strokeWidth = 2.dp.toPx())
}