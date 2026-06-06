package com.neuroproject.neuro.presentation.screens.devicesearch

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.domain.model.DeviceConnectionState
import com.neuroproject.neuro.domain.model.DeviceInfo
import com.neuroproject.neuro.ui.theme.NeuroApplicationTheme
import com.neuroproject.neuro.ui.theme.ThemeMode

// ============================================================
// REAL SCREEN (с Hilt)
// ============================================================

@Composable
fun DeviceSearchScreen(
    modifier: Modifier = Modifier,
    vm: DeviceSearchScreenViewModel = hiltViewModel(),
    onDeviceConnected: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val uiState by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.connectionState) {
        if (uiState.connectionState == DeviceConnectionState.connected) {
            onDeviceConnected()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            vm.clearError()
        }
    }

    DeviceSearchScreenContent(
        uiState = uiState,
        onStartSearch = { vm.startSearch() },
        onRetrySearch = { vm.retrySearch() },
        onConnect = { device -> vm.connect(device.address) },
        onClearError = { vm.clearError() }
    )
}

// ============================================================
// PURE UI COMPONENT (для превью и переиспользования)
// ============================================================

@Composable
fun DeviceSearchScreenContent(
    uiState: DeviceSearchScreenState,
    onStartSearch: () -> Unit,
    onRetrySearch: () -> Unit,
    onConnect: (DeviceInfo) -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            onClearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                SearchHeader()

                Spacer(modifier = Modifier.height(24.dp))

                DividerLine()

                Spacer(modifier = Modifier.height(16.dp))

                SearchStatusSection(
                    isSearchTimeout = uiState.isSearchTimeout,
                    connectionState = uiState.connectionState,
                    isSearching = uiState.isSearching,
                    foundDevices = uiState.foundDevices,
                    onRetrySearch = onRetrySearch,
                    onStartSearch = onStartSearch
                )

                Spacer(modifier = Modifier.height(24.dp))

                DevicesList(
                    devices = uiState.foundDevices,
                    connectionState = uiState.connectionState,
                    connectingDeviceId = uiState.connectingDeviceId,
                    isSearching = uiState.isSearching,
                    isSearchTimeout = uiState.isSearchTimeout,
                    onConnect = onConnect
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

// ============================================================
// UI COMPONENTS
// ============================================================

@Composable
private fun SearchHeader() {
    Text(
        text = "Поиск устройств",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun SearchStatusSection(
    isSearchTimeout: Boolean,
    connectionState: DeviceConnectionState,
    isSearching: Boolean,
    foundDevices: List<DeviceInfo>,
    onRetrySearch: () -> Unit,
    onStartSearch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            isSearchTimeout -> {
                TimeoutContent(onRetrySearch = onRetrySearch)
            }
            connectionState == DeviceConnectionState.connecting -> {
                StatusText("Подключение...", MaterialTheme.colorScheme.primary)
            }
            connectionState == DeviceConnectionState.connected -> {
                StatusText("Устройство подключено", MaterialTheme.colorScheme.primary)
            }
            connectionState == DeviceConnectionState.error -> {
                StatusText("Ошибка подключения", MaterialTheme.colorScheme.error)
            }
            isSearching -> {
                SearchingContent()
            }
            !isSearching && foundDevices.isEmpty() -> {
                StartSearchButton(onStartSearch = onStartSearch)
            }
        }
    }
}

@Composable
private fun TimeoutContent(onRetrySearch: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StatusText(
            text = "Время поиска истекло",
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetrySearch,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Повторить поиск")
        }
    }
}

@Composable
private fun StatusText(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color
    )
}

@Composable
private fun SearchingContent() {
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

@Composable
private fun StartSearchButton(onStartSearch: () -> Unit) {
    Button(
        onClick = onStartSearch,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("Начать поиск")
    }
}

@Composable
private fun DevicesList(
    devices: List<DeviceInfo>,
    connectionState: DeviceConnectionState,
    connectingDeviceId: String?,
    isSearching: Boolean,
    isSearchTimeout: Boolean,
    onConnect: (DeviceInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp)
    ) {
        when {
            devices.isEmpty() && !isSearching && !isSearchTimeout -> {
                item { EmptyDevicesMessage("Нажмите 'Начать поиск' для сканирования устройств") }
            }
            devices.isEmpty() && isSearching -> {
                item { EmptyDevicesMessage("Поиск устройств...\nУбедитесь, что Bluetooth включен") }
            }
            devices.isEmpty() -> {
                item {
                    EmptyDevicesMessage(
                        text = "Устройства не найдены за 30 секунд\n" +
                                "Проверьте:\n" +
                                "• Bluetooth включен\n" +
                                "• Устройство рядом\n" +
                                "• Устройство в режиме сопряжения"
                    )
                }
            }
            else -> {
                items(devices, key = { it.address }) { device ->
                    val isThisDeviceConnecting = connectingDeviceId == device.address &&
                            connectionState == DeviceConnectionState.connecting
                    val isThisDeviceConnected = connectionState == DeviceConnectionState.connected &&
                            device.address == connectingDeviceId
                    val isEnabled = connectionState == DeviceConnectionState.disconnected &&
                            connectingDeviceId == null

                    DeviceItem(
                        device = device,
                        isConnecting = isThisDeviceConnecting,
                        isConnected = isThisDeviceConnected,
                        isEnabled = isEnabled,
                        onClick = { onConnect(device) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDevicesMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
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
                        text = device.name.ifEmpty { "Неизвестное устройство" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            isConnected -> MaterialTheme.colorScheme.primary
                            isConnecting -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DeviceStateIndicator(
                isConnecting = isConnecting,
                isConnected = isConnected
            )
        }
    }
}

@Composable
private fun DeviceStateIndicator(
    isConnecting: Boolean,
    isConnected: Boolean
) {
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

// ============================================================
// PREVIEWS
// ============================================================

@Preview(
    name = "Светлая тема - список устройств",
    showBackground = true,
    device = Devices.PIXEL_4
)
@Composable
fun PreviewDeviceListLight() {
    NeuroApplicationTheme(
        themeMode = ThemeMode.LIGHT,
        dynamicColor = false
    ) {
        DeviceSearchScreenContent(
            uiState = DeviceSearchScreenState(
                foundDevices = listOf(
                    DeviceInfo(name = "Capsule-1234", address = "AA:BB:CC:DD:EE:01"),
                    DeviceInfo(name = "Capsule-5678", address = "AA:BB:CC:DD:EE:02"),
                    DeviceInfo(name = "NeuroBand-9012", address = "AA:BB:CC:DD:EE:03")
                ),
                isSearching = false,
                isSearchTimeout = false,
                connectionState = DeviceConnectionState.disconnected
            ),
            onStartSearch = {},
            onRetrySearch = {},
            onConnect = {},
            onClearError = {}
        )
    }
}

@Preview(
    name = "Тёмная тема - список устройств",
    showBackground = true,
    device = Devices.PIXEL_4
)
@Composable
fun PreviewDeviceListDark() {
    NeuroApplicationTheme(
        themeMode = ThemeMode.DARK,
        dynamicColor = false
    ) {
        DeviceSearchScreenContent(
            uiState = DeviceSearchScreenState(
                foundDevices = listOf(
                    DeviceInfo(name = "Capsule-1234", address = "AA:BB:CC:DD:EE:01"),
                    DeviceInfo(name = "Capsule-5678", address = "AA:BB:CC:DD:EE:02")
                ),
                isSearching = false,
                isSearchTimeout = false,
                connectionState = DeviceConnectionState.disconnected
            ),
            onStartSearch = {},
            onRetrySearch = {},
            onConnect = {},
            onClearError = {}
        )
    }
}

@Preview(
    name = "Светлая тема - поиск",
    showBackground = true
)
@Composable
fun PreviewSearchingLight() {
    NeuroApplicationTheme(
        themeMode = ThemeMode.LIGHT,
        dynamicColor = false
    ) {
        DeviceSearchScreenContent(
            uiState = DeviceSearchScreenState(
                foundDevices = emptyList(),
                isSearching = true,
                isSearchTimeout = false,
                connectionState = DeviceConnectionState.disconnected
            ),
            onStartSearch = {},
            onRetrySearch = {},
            onConnect = {},
            onClearError = {}
        )
    }
}

@Preview(
    name = "Светлая тема - подключение",
    showBackground = true
)
@Composable
fun PreviewConnectingLight() {
    NeuroApplicationTheme(
        themeMode = ThemeMode.LIGHT,
        dynamicColor = false
    ) {
        DeviceSearchScreenContent(
            uiState = DeviceSearchScreenState(
                foundDevices = listOf(
                    DeviceInfo(name = "Capsule-1234", address = "AA:BB:CC:DD:EE:01")
                ),
                isSearching = false,
                isSearchTimeout = false,
                connectionState = DeviceConnectionState.connecting,
                connectingDeviceId = "AA:BB:CC:DD:EE:01"
            ),
            onStartSearch = {},
            onRetrySearch = {},
            onConnect = {},
            onClearError = {}
        )
    }
}

@Preview(
    name = "Светлая тема - подключено",
    showBackground = true
)
@Composable
fun PreviewConnectedLight() {
    NeuroApplicationTheme(
        themeMode = ThemeMode.LIGHT,
        dynamicColor = false
    ) {
        DeviceSearchScreenContent(
            uiState = DeviceSearchScreenState(
                foundDevices = listOf(
                    DeviceInfo(name = "Capsule-1234", address = "AA:BB:CC:DD:EE:01")
                ),
                isSearching = false,
                isSearchTimeout = false,
                connectionState = DeviceConnectionState.connected,
                connectingDeviceId = "AA:BB:CC:DD:EE:01"
            ),
            onStartSearch = {},
            onRetrySearch = {},
            onConnect = {},
            onClearError = {}
        )
    }
}

@Preview(
    name = "Светлая тема - таймаут",
    showBackground = true
)
@Composable
fun PreviewTimeoutLight() {
    NeuroApplicationTheme(
        themeMode = ThemeMode.LIGHT,
        dynamicColor = false
    ) {
        DeviceSearchScreenContent(
            uiState = DeviceSearchScreenState(
                foundDevices = emptyList(),
                isSearching = false,
                isSearchTimeout = true,
                connectionState = DeviceConnectionState.disconnected
            ),
            onStartSearch = {},
            onRetrySearch = {},
            onConnect = {},
            onClearError = {}
        )
    }
}

@Preview(
    name = "Светлая тема - ошибка",
    showBackground = true
)
@Composable
fun PreviewErrorLight() {
    NeuroApplicationTheme(
        themeMode = ThemeMode.LIGHT,
        dynamicColor = false
    ) {
        DeviceSearchScreenContent(
            uiState = DeviceSearchScreenState(
                foundDevices = emptyList(),
                isSearching = false,
                isSearchTimeout = false,
                connectionState = DeviceConnectionState.error,
                errorMessage = "Ошибка подключения к устройству"
            ),
            onStartSearch = {},
            onRetrySearch = {},
            onConnect = {},
            onClearError = {}
        )
    }
}