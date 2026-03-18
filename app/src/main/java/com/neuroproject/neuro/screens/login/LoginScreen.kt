package com.neuroproject.neuro.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neuroproject.neuro.R

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vm: LoginScreenViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = { },
    onBackPressed: () -> Unit = {}
) {
    val loginState by vm.loginState.collectAsState()
    val userId by remember { vm.userId }
    val errorMessage by remember { vm.errorMessage }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Connected) {
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

                when (val state = loginState) {
                    is LoginState.Connecting -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp).aspectRatio(1f),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Выполняется вход...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is LoginState.Connected -> {
                        Text(
                            text = "Вход выполнен успешно!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is LoginState.Error -> {
                        Text(
                            text = "Ошибка входа",
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

                OutlinedTextField(
                    value = userId,
                    onValueChange = vm::onUserIdChange,
                    label = { Text("ID пользователя *") },
                    placeholder = { Text("Введите ID") },
                    isError = userId.length < 3 && userId.isNotEmpty(),
                    supportingText = {
                        if (userId.isNotEmpty()) {
                            Text(
                                text = if (userId.length >= 3) "✓ Минимум 3 символа" else "✗ Минимум 3 символа",
                                color = if (userId.length >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                if (vm.hasSavedData()) {
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
                    enabled = userId.length >= 3 && loginState !is LoginState.Connecting,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    when {
                        loginState is LoginState.Connecting -> {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp).aspectRatio(1f),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        loginState is LoginState.Connected -> {
                            Text("Успешно")
                        }
                        else -> {
                            Text("Войти")
                        }
                    }
                }

                if (vm.hasSavedData() && loginState !is LoginState.Connecting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = vm::clearSavedData) {
                        Text(
                            text = "Очистить сохраненный ID",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}