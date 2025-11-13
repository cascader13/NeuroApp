package com.neuroproject.neuro.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
//Заглушка, надо будет доделать
fun MainScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Главный экран",
            color = Color.White,
            fontSize = 24.sp
        )
    }
}