package com.neuroproject.neuro.presentation.screens.blank

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BlankScreen(
    modifier: Modifier = Modifier,
    vm: BlankViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onDeviceUnconnected: () -> Unit = {}
) {


}

