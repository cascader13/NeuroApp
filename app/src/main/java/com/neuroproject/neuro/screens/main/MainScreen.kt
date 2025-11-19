package com.neuroproject.neuro.screens.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.services.NFBData

/**
 * Главный экран приложения для отображения нейрофидбэк данных
 * Отображает графики мозговых волн в реальном времени
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel()
) {
    // Сбор данных из ViewModel
    val plotData by vm.plotData.collectAsState() // Данные для построения графиков
    val nfbData by vm.nfb.collectAsState() // Текущие значения нейрофидбэк данных

    // Основной контейнер экрана
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Черный фон для лучшего контраста
    ) {
        // Верхняя секция с заголовком и текущими значениями
        HeaderSection(nfbData = nfbData, onClear = { vm.clearPlotData() })

        // Секция с графиками мозговых волн
        NeuroGraphs(
            plotData = plotData,
            modifier = Modifier
                .weight(1f) // Занимает все доступное пространство
                .padding(16.dp)
        )

        // Нижняя секция с легендой графиков
        LegendSection()
    }
}

/**
 * Верхняя секция экрана с заголовком и текущими значениями волн
 */
@Composable
private fun HeaderSection(nfbData: NFBData, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Строка с заголовком и кнопкой очистки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Заголовок экрана
            Text(
                text = "Мониторинг",
                color = Color.White,
                fontSize = 20.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            // Кнопка очистки данных графиков
            Button(
                onClick = onClear,
                modifier = Modifier.height(36.dp)
            ) {
                Text("Очистить", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Строка с текущими значениями мозговых волн
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround
        ) {
            // Отображение значений Alpha, Beta, Delta волн
            ValueDisplay(label = "Alpha", value = nfbData.alpha, color = Color(0xFF4FC3F7))
            ValueDisplay(label = "Beta", value = nfbData.beta, color = Color(0xFFF44336))
            ValueDisplay(label = "Delta", value = nfbData.delta, color = Color(0xFF66BB6A))
        }
    }
}

/**
 * Компонент для отображения отдельного значения мозговой волны
 */
@Composable
private fun ValueDisplay(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Название волны
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f), // Полупрозрачный белый
            fontSize = 12.sp
        )
        // Числовое значение волны
        Text(
            text = "%.2f".format(value), // Форматирование до 2 знаков после запятой
            color = color, // Цвет соответствует типу волны
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Контейнер для всех графиков мозговых волн
 */
@Composable
private fun NeuroGraphs(
    plotData: PlotData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // График Alpha волн
        NeuroGraph(
            points = plotData.alphaPoints,
            color = Color(0xFF4FC3F7), // Голубой цвет
            label = "Alpha Waves",
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
        )

        // График Beta волн
        NeuroGraph(
            points = plotData.betaPoints,
            color = Color(0xFFF44336), // Красный цвет
            label = "Beta Waves",
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
        )

        // График Delta волн
        NeuroGraph(
            points = plotData.deltaPoints,
            color = Color(0xFF66BB6A), // Зеленый цвет
            label = "Delta Waves",
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
        )
    }
}

/**
 * Отдельный график для отображения мозговой волны
 */
@Composable
private fun NeuroGraph(
    points: List<DataPoint>,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Заголовок графика
        Text(
            text = label,
            color = color,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Canvas для рисования графика
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A)) // Темно-серый фон для графика
        ) {
            // Проверка наличия достаточного количества данных
            if (points.size < 2) {
                drawPlaceholder("Ожидание данных...")
                return@Canvas
            }

            // Расчет размеров области графика
            val padding = 24.dp.toPx()
            val graphWidth = size.width - 2 * padding
            val graphHeight = size.height - 2 * padding

            // Вычисление границ данных для масштабирования
            val bounds = calculateBounds(points)

            // Отрисовка сетки координат
            drawGrid(padding, graphWidth, graphHeight, bounds)

            // Отрисовка линии графика
            drawWaveLine(points, color, padding, graphWidth, graphHeight, bounds)
        }
    }
}

/**
 * Отрисовка заглушки при отсутствии данных
 */
private fun DrawScope.drawPlaceholder(message: String) {
    // Простой круг вместо текста (так как рисование текста в Canvas сложнее)
    drawCircle(
        color = Color.White.copy(alpha = 0.1f), // Полупрозрачный белый
        center = Offset(size.width / 2, size.height / 2), // Центр Canvas
        radius = 20.dp.toPx()
    )
}

/**
 * Расчет границ данных для правильного масштабирования графика
 */
private fun calculateBounds(points: List<DataPoint>): WaveBounds {
    if (points.isEmpty()) return WaveBounds(0f, 1f, 0f, 1f) // Значения по умолчанию

    val xValues = points.map { it.x }
    val yValues = points.map { it.y }

    val minX = xValues.minOrNull() ?: 0f
    val maxX = xValues.maxOrNull() ?: 1f
    val minY = 0f // Фиксируем нижнюю границу на 0 для лучшей визуализации
    val maxY = (yValues.maxOrNull() ?: 1f) * 1.1f // Добавляем 10% сверху для запаса

    return WaveBounds(minX, maxX, minY, maxY)
}

/**
 * Data class для хранения границ данных графика
 */
private data class WaveBounds(
    val minX: Float, // Минимальное значение по X
    val maxX: Float, // Максимальное значение по X
    val minY: Float, // Минимальное значение по Y
    val maxY: Float  // Максимальное значение по Y
) {
    val xRange: Float get() = maxX - minX // Диапазон по X
    val yRange: Float get() = maxY - minY // Диапазон по Y
}

/**
 * Отрисовка координатной сетки графика
 */
private fun DrawScope.drawGrid(
    padding: Float,
    graphWidth: Float,
    graphHeight: Float,
    bounds: WaveBounds
) {
    // Горизонтальная ось (время)
    drawLine(
        start = Offset(padding, size.height - padding),
        end = Offset(size.width - padding, size.height - padding),
        color = Color.White.copy(alpha = 0.3f), // Полупрозрачный белый
        strokeWidth = 1.dp.toPx()
    )

    // Вертикальная ось (амплитуда)
    drawLine(
        start = Offset(padding, padding),
        end = Offset(padding, size.height - padding),
        color = Color.White.copy(alpha = 0.3f),
        strokeWidth = 1.dp.toPx()
    )
}

/**
 * Отрисовка линии графика и точек данных
 */
private fun DrawScope.drawWaveLine(
    points: List<DataPoint>,
    color: Color,
    padding: Float,
    graphWidth: Float,
    graphHeight: Float,
    bounds: WaveBounds
) {
    // Создание пути для линии графика
    val path = Path().apply {
        points.forEachIndexed { index, point ->
            // Преобразование данных в координаты Canvas
            val x = padding + ((point.x - bounds.minX) / bounds.xRange) * graphWidth
            val y = size.height - padding - ((point.y - bounds.minY) / bounds.yRange) * graphHeight

            if (index == 0) {
                moveTo(x, y) // Начальная точка
            } else {
                lineTo(x, y) // Соединение точек
            }
        }
    }

    // Отрисовка плавной линии графика
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round // Скругленные концы линий
        )
    )

    // Отрисовка точек для последних 10 значений (визуальное выделение новых данных)
    points.takeLast(10).forEach { point ->
        val x = padding + ((point.x - bounds.minX) / bounds.xRange) * graphWidth
        val y = size.height - padding - ((point.y - bounds.minY) / bounds.yRange) * graphHeight

        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

/**
 * Секция с легендой графиков в нижней части экрана
 */
@Composable
private fun LegendSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround
    ) {
        LegendItem(color = Color(0xFF4FC3F7), text = "Alpha")
        LegendItem(color = Color(0xFFF44336), text = "Beta")
        LegendItem(color = Color(0xFF66BB6A), text = "Delta")
    }
}

/**
 * Элемент легенды с цветным квадратом и подписью
 */
@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Цветной квадрат
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Текст подписи
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}