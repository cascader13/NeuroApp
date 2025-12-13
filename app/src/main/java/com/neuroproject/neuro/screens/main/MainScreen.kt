package com.neuroproject.neuro.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onStartSessionClick: () -> Unit = {},
    onViewResultsClick: () -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Верхняя панель с кнопкой настроек
        TopBar(
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Заголовок приложения
        AppTitle()

        Spacer(modifier = Modifier.height(60.dp))

        // Основное меню с кнопками
        MenuButtons(
            onStartSessionClick = onStartSessionClick,
            onViewResultsClick = onViewResultsClick
        )
    }
}

@Composable
private fun TopBar(
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        // Кнопка настроек
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onSettingsClick)
                .background(Color(0xFF2A2A2A))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AppTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Логотип или иконка
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color(0xFF4FC3F7)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NFB",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Название приложения
        Text(
            text = "NeuroAssestment",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MenuButtons(
    onStartSessionClick: () -> Unit,
    onViewResultsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Кнопка "Начать сессию"
        MenuButton(
            title = "Начать сессию",
            subtitle = "",
            backgroundColor = Color(0xFF4FC3F7),
            onClick = onStartSessionClick
        )

        // Кнопка "Предыдущие результаты"
        MenuButton(
            title = "Предыдущие результаты",
            subtitle = "Просмотр истории",
            backgroundColor = Color(0xFF2A2A2A),
            onClick = onViewResultsClick
        )
    }
}

@Composable
private fun MenuButton(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}