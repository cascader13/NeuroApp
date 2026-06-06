package com.neuroproject.neuro.presentation.screens.login

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R

object LoginScreenTags {
    const val UserIdField = "login_user_id_field"
    const val LoginButton = "login_button"
    const val ClearSavedDataButton = "clear_saved_data_button"
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vm: LoginScreenViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = { },
    onBackPressed: () -> Unit = {}
) {
    val uiState by vm.state.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    LoginScreenContent(
        modifier = modifier,
        uiState = uiState,
        onUserIdChange = vm::onUserIdChange,
        onLoginClick = vm::login,
        onClearSavedData = vm::clearSavedData,
        onRemoveFromHistory = vm::removeFromHistory,
        onSelectFromHistory = vm::selectUserIdFromHistory,
        onFieldFocusChange = vm::onTextFieldFocusChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    uiState: LoginUIState,
    onUserIdChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onClearSavedData: () -> Unit,
    onRemoveFromHistory: (String) -> Unit,
    onSelectFromHistory: (String) -> Unit,
    onFieldFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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

                val statusText = when {
                    uiState.isLoggedIn -> "Вход выполнен успешно!"
                    uiState.errorMessage != null -> uiState.errorMessage
                    else -> "Введите ID пользователя"
                }
                Text(
                    text = statusText ?: "Ошибка входа",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        uiState.isLoggedIn -> MaterialTheme.colorScheme.primary
                        uiState.errorMessage != null -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded && uiState.userIdHistory.isNotEmpty(),
                    onExpandedChange = { expanded = it && uiState.userIdHistory.isNotEmpty() }
                ) {
                    OutlinedTextField(
                        value = uiState.userId,
                        onValueChange = onUserIdChange,
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
                            .testTag(LoginScreenTags.UserIdField)
                            .onFocusEvent { focusState ->
                                onFieldFocusChange(focusState.isFocused)
                                expanded = focusState.isFocused && uiState.userIdHistory.isNotEmpty()
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
                            uiState.userIdHistory.filter { it.contains(uiState.userId, ignoreCase = true) }
                        } else {
                            uiState.userIdHistory
                        }

                        if (filteredHistory.isEmpty()) {
                            DropdownMenuItem(text = { Text("Нет совпадений") }, onClick = { })
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
                                            IconButton(
                                                onClick = {
                                                    onRemoveFromHistory(userId)
                                                    if (uiState.userIdHistory.size <= 1) expanded = false
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
                                        onSelectFromHistory(userId)
                                        expanded = false
                                    }
                                )
                                if (userId != filteredHistory.last()) Divider()
                            }

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
                                        uiState.userIdHistory.forEach(onRemoveFromHistory)
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
                    onClick = onLoginClick,
                    enabled = uiState.isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag(LoginScreenTags.LoginButton)
                ) {
                    Text("Войти")
                }

                if (uiState.hasSavedData) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onClearSavedData,
                        modifier = Modifier.testTag(LoginScreenTags.ClearSavedDataButton)
                    ) {
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
