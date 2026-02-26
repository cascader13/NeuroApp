package com.neuroproject.neuro.screens.devicesearch

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.models.DeviceInfo
import com.neuroproject.neuro.services.DeviceConnectionState

@Composable
fun DeviceSearchScreen(
    modifier: Modifier = Modifier,
    vm: DeviceSearchScreenViewModel,
    onDeviceConnected: () -> Unit = { },
    onBackPressed: () -> Unit = {}
) {
    var connectingDeviceId by remember { mutableStateOf<String?>(null) }
    val foundedSensors = vm.foundDevices.collectAsState()
    val connectionState = vm.deviceState.collectAsState()
    val isSearchTimeout by vm.isSearchTimeout.collectAsState()
    val isSearching by vm.isSearching.collectAsState()

    // Сбрасываем connectingDeviceId при изменении состояния подключения
    LaunchedEffect(connectionState.value) {
        when (connectionState.value) {
            DeviceConnectionState.connected -> {
                connectingDeviceId = null
                onDeviceConnected()
            }
            DeviceConnectionState.disconnected -> {
                connectingDeviceId = null
            }
            else -> { /* Ignore */ }
        }
    }

    Surface(
        color = Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column {
            Spacer(Modifier.height(24.dp))

            Column(Modifier.padding(horizontal = 12.dp)) {
                Text(
                    "Поиск устройств",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Box(
                    Modifier
                        .height(2.dp)
                        .fillMaxWidth()
                        .background(Color.White)
                        .shadow(
                            20.dp,
                            RoundedCornerShape(30.dp),
                            ambientColor = Color.White,
                            spotColor = Color.White
                        )
                )

                Spacer(Modifier.height(16.dp))

                // Статус поиска
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        isSearchTimeout -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Время поиска истекло",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { vm.retrySearch() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Повторить поиск")
                                }
                            }
                        }
                        connectionState.value == DeviceConnectionState.connection -> {
                            Text(
                                "Подключение...",
                                color = Color.Yellow,
                                fontSize = 16.sp
                            )
                        }
                        connectionState.value == DeviceConnectionState.connected -> {
                            Text(
                                "Устройство подключено",
                                color = Color.Green,
                                fontSize = 16.sp
                            )
                        }
                        isSearching -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .aspectRatio(1f),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    "Идет поиск устройств...",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        !isSearching && foundedSensors.value.isEmpty() -> {
                            Button(
                                onClick = { vm.startSearch() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Начать поиск")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Список найденных устройств
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxHeight()
                ) {
                    if (foundedSensors.value.isEmpty() && !isSearching && !isSearchTimeout) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Нажмите 'Начать поиск' для сканирования устройств",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    } else if (foundedSensors.value.isEmpty() && isSearching) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Поиск устройств...\nУбедитесь, что Bluetooth включен",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    } else if (foundedSensors.value.isEmpty() && isSearchTimeout) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Устройства не найдены за 30 секунд\n" +
                                            "Проверьте:\n" +
                                            "• Bluetooth включен\n" +
                                            "• Устройство рядом\n" +
                                            "• Устройство в режиме сопряжения",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }

                    items(foundedSensors.value, key = { it.id }) { device ->
                        val isThisDeviceConnecting = connectingDeviceId == device.id &&
                                connectionState.value == DeviceConnectionState.connection
                        val isThisDeviceConnected = connectionState.value == DeviceConnectionState.connected &&
                                device.id == connectingDeviceId

                        DeviceItem(
                            device = device,
                            isConnecting = isThisDeviceConnecting,
                            isConnected = isThisDeviceConnected,
                            isEnabled = connectionState.value == DeviceConnectionState.disconnected &&
                                    !isThisDeviceConnecting &&
                                    connectingDeviceId == null,
                            onClick = {
                                connectingDeviceId = device.id
                                vm.connect(device.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: DeviceInfo,
    isConnecting: Boolean,
    isConnected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Devices,
                    contentDescription = "Устройство",
                    tint = when {
                        isConnected -> Color.Green
                        isConnecting -> Color.Yellow
                        else -> Color.White
                    }
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = device.description.ifEmpty { "Неизвестное устройство" },
                        color = when {
                            isConnected -> Color.Green
                            isConnecting -> Color.Yellow
                            else -> Color.White
                        },
                        fontSize = 18.sp
                    )
                    Text(
                        text = device.id,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Индикатор состояния
            when {
                isConnecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .aspectRatio(1f),
                        color = Color.Yellow,
                        strokeWidth = 2.dp
                    )
                }
                isConnected -> {
                    Text(
                        "✓ Подключено",
                        color = Color.Green,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    DeviceSearchScreen(modifier = Modifier, hiltViewModel())
}