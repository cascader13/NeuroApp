package com.neuroproject.neuro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import com.neuroproject.neuro.ui.theme.ThemeViewModel

@Composable
fun NeuroApplication(modifier: Modifier = Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val themeMode by themeViewModel.themeMode.collectAsState()

    NeuroApplicationTheme(
        themeMode = themeMode,
        dynamicColor = false
    ) {
        NeuroNavGraph(modifier = modifier)
    }
}