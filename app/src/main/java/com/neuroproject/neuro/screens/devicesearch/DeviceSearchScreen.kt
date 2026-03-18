package com.neuroproject.neuro.screens.devicesearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val foundedSensors by vm.foundDevices.collectAsState()
    val connectionState by vm.deviceState.collectAsState()
    val isSearchTimeout by vm.isSearchTimeout.collectAsState()
    val isSearching by vm.isSearching.collectAsState()

    // Сбрасываем connectingDeviceId при изменении состояния подключения
    LaunchedEffect(connectionState) {
        when (connectionState) {
            DeviceConnectionState.connected -> {
                connectingDeviceId = null
                onDeviceConnected()
            }

            DeviceConnectionState.disconnected -> {
                connectingDeviceId = null
            }

            else -> { /* Ignore */
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Поиск устройств",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Разделитель
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.outline)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                                text = "Время поиска истекло",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { vm.retrySearch() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Повторить поиск")
                            }
                        }
                    }

                    connectionState == DeviceConnectionState.connection -> {
                        Text(
                            text = "Подключение...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    connectionState == DeviceConnectionState.connected -> {
                        Text(
                            text = "Устройство подключено",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
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
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Идет поиск устройств...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    !isSearching && foundedSensors.isEmpty() -> {
                        Button(
                            onClick = { vm.startSearch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Начать поиск")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Список найденных устройств
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
            ) {
                if (foundedSensors.isEmpty() && !isSearching && !isSearchTimeout) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нажмите 'Начать поиск' для сканирования устройств",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (foundedSensors.isEmpty() && isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Поиск устройств...\nУбедитесь, что Bluetooth включен",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                            )
                        }
                    }
                } else if (foundedSensors.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Устройства не найдены за 30 секунд\n" +
                                        "Проверьте:\n" +
                                        "• Bluetooth включен\n" +
                                        "• Устройство рядом\n" +
                                        "• Устройство в режиме сопряжения",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(foundedSensors, key = { it.id }) { device ->
                    val isThisDeviceConnecting = connectingDeviceId == device.id &&
                            connectionState == DeviceConnectionState.connection
                    val isThisDeviceConnected =
                        connectionState == DeviceConnectionState.connected &&
                                device.id == connectingDeviceId

                    DeviceItem(
                        device = device,
                        isConnecting = isThisDeviceConnecting,
                        isConnected = isThisDeviceConnected,
                        isEnabled = connectionState == DeviceConnectionState.disconnected && connectingDeviceId == null,
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
                        isConnected -> MaterialTheme.colorScheme.primary
                        isConnecting -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = device.description.ifEmpty { "Неизвестное устройство" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            isConnected -> MaterialTheme.colorScheme.primary
                            isConnecting -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = device.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                }

                isConnected -> {
                    Text(
                        text = "✓ Подключено",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}