package com.neuroproject.neuro.presentation.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * Экран входа в приложение по ID пользователя.
 */
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
        onSelectFromHistory = vm::selectUserIdFromHistory
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUIState,
    onUserIdChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onClearSavedData: () -> Unit,
    onRemoveFromHistory: (String) -> Unit,
    onSelectFromHistory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Логотип приложения",
                modifier = Modifier.size(300.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
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
            )

            if (uiState.userIdHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.userIdHistory) { userId ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onSelectFromHistory(userId) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = userId,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onRemoveFromHistory(userId) },
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
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
