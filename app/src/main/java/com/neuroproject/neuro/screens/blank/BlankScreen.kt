package com.neuroproject.neuro.screens.blank

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.services.DeviceConnectionState

@Composable
fun BlankScreen(
    modifier: Modifier = Modifier,
    vm: BlankViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onDeviceUnconnected: () -> Unit = {}
) {
    val connectionState by vm.dm.connectionState.collectAsState()

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
}

