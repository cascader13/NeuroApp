package com.neuroproject.neuro.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vm: LoginScreenViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = { },
    onBackPressed: () -> Unit = {}
) {
    val uiState by vm.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Логотип приложения",
                    modifier = Modifier.size(300.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Вход в систему",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                when {
                    uiState.isLoggedIn -> {
                        Text(
                            text = "Вход выполнен успешно!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.errorMessage != null -> {
                        Text(
                            text = uiState.errorMessage ?: "Ошибка входа",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        Text(
                            text = "Введите ID пользователя",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))


                ExposedDropdownMenuBox(
                    expanded = expanded && uiState.userIdHistory.isNotEmpty(),
                    onExpandedChange = {
                        expanded = it && uiState.userIdHistory.isNotEmpty()
                    }
                ) {
                    OutlinedTextField(
                        value = uiState.userId,
                        onValueChange = { newValue ->
                            vm.onUserIdChange(newValue)
                            if (newValue.isNotEmpty() && uiState.userIdHistory.isNotEmpty()) {
                                expanded = true
                            }
                        },
                        label = { Text("ID пользователя *") },
                        placeholder = { Text("Введите ID или выберите из истории") },
                        isError = !uiState.isValid && uiState.userId.isNotEmpty(),
                        supportingText = {
                            if (uiState.userId.isNotEmpty()) {
                                Text(
                                    text = if (uiState.isValid) "✓ Корректный ID" else "✗ ID должен содержать минимум 2 символа (буквы, цифры, -, _)",
                                    color = if (uiState.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused && uiState.userIdHistory.isNotEmpty()) {
                                    expanded = true
                                } else if (!focusState.isFocused) {
                                    expanded = false
                                }
                            }
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded && uiState.userIdHistory.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        val filteredHistory = if (uiState.userId.isNotEmpty()) {
                            uiState.userIdHistory.filter {
                                it.contains(uiState.userId, ignoreCase = true)
                            }
                        } else {
                            uiState.userIdHistory
                        }

                        if (filteredHistory.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Нет совпадений",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { }
                            )
                        } else {
                            filteredHistory.forEach { userId ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = userId,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            // Кнопка удаления из истории
                                            IconButton(
                                                onClick = {
                                                    vm.removeFromHistory(userId)
                                                    if (uiState.userIdHistory.size <= 1) {
                                                        expanded = false
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Удалить",
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        vm.selectUserIdFromHistory(userId)
                                        expanded = false
                                    }
                                )

                                if (userId != filteredHistory.last()) {
                                    Divider()
                                }
                            }

                            // Опция очистки всей истории
                            if (uiState.userIdHistory.size > 1) {
                                Divider()
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Очистить всю историю",
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    onClick = {
                                        uiState.userIdHistory.forEach { userId ->
                                            vm.removeFromHistory(userId)
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (uiState.hasSavedData) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Используется сохраненный ID",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = vm::login,
                    enabled = uiState.isValid,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Войти")
                }

                if (uiState.hasSavedData) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = vm::clearSavedData) {
                        Text(
                            text = "Очистить сохраненные данные",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}