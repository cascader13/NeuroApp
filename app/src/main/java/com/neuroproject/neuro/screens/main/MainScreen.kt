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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onStartSessionClick: () -> Unit = {},
    onViewResultsClick: () -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel()
) {
    Surface(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopBar(onSettingsClick = onSettingsClick)
            Spacer(modifier = Modifier.height(20.dp))
            AppTitle()
            Spacer(modifier = Modifier.height(100.dp))
            MenuButtons(
                onStartSessionClick = onStartSessionClick,
                onViewResultsClick = onViewResultsClick
            )
        }
    }
}

@Composable
private fun TopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Настройки",
                tint = MaterialTheme.colorScheme.onSurface
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
        // Логотип
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Логотип приложения",
            modifier = Modifier.size(300.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "НейроСтат",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
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
        MenuButton(
            title = "Начать сессию",
            subtitle = "",
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onStartSessionClick
        )

        MenuButton(
            title = "Предыдущие результаты",
            subtitle = "",
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onViewResultsClick
        )
    }
}

@Composable
private fun MenuButton(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column {
            Text(
                text = title,
                color = contentColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = contentColor.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen() {
    NeuroApplicationTheme {
        MainScreen(
            onSettingsClick = {},
            onStartSessionClick = {},
            onViewResultsClick = {}
        )
    }
}