package com.neuroproject.neuro.screens.settings


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    vm: SettingsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()


    SettingsScreenContent(
        modifier = modifier,
        state = state,
        onBackClick = onBackClick,
        onKeyChanged = vm::onKeyChanged,
        onUploadClicked = vm::onUploadClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    onBackClick: () -> Unit,
    onKeyChanged: (String) -> Unit,
    onUploadClicked: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Невидимый прогресс-бар (становится видимым при загрузке)
            InvisibleProgressBar(
                isVisible = state.isUploading,
                progress = state.uploadProgress
            )

            // Поле для ввода  ключа
            KeyField(
                Key = state.key,
                isKeyValid = state.isKeyValid,
                onValueChange = onKeyChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка выгрузки на сервер
            UploadButton(
                isLoading = state.isUploading,
                isEnabled = state.isKeyValid && !state.isUploading,
                onClick = onUploadClicked
            )

            // Состояние загрузки с прогрессом
            if (state.isUploading) {
                UploadProgressStatus(
                    progress = state.uploadProgress
                )
            }

            // Сообщение об ошибке
            state.errorMessage?.let { errorMessage ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF5252).copy(alpha = 0.2f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp
                    )
                }
            }

            // Сообщение об успехе
            state.successMessage?.let { successMessage ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = successMessage,
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Информация о версии
            Text(
                text = state.appInfo,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun InvisibleProgressBar(
    isVisible: Boolean,
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .alpha(if (isVisible) 1f else 0f) // Контролируем видимость через alpha
    ) {
        LinearProgressIndicator(
            progress = {progress},
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color(0xFF4FC3F7),
            strokeCap = StrokeCap.Square
        )
    }
}

@Composable
private fun UploadProgressStatus(
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Выгрузка данных...",
            color = Color(0xFF4FC3F7),
            fontSize = 16.sp
        )

        Text(
            text = "${(progress * 100).toInt()}%",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        // Дополнительная информация о процессе
        when {
            progress < 0.3f -> Text(
                text = "Подготовка данных...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            progress < 0.7f -> Text(
                text = "Отправка на сервер...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            else -> Text(
                text = "Завершение...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyField(
    Key: String,
    isKeyValid: Boolean,
    onValueChange: (String) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ключ",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = Key,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (isKeyValid) Color(0xFF4FC3F7) else Color(0xFFFF5252),
                unfocusedBorderColor = Color(0xFF2A2A2A),
                focusedLabelColor = Color(0xFF4FC3F7),
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                containerColor = Color(0xFF1A1A1A),
                cursorColor = Color(0xFF4FC3F7)
            ),
            placeholder = {
                Text(
                    text = "Введите ваш ключ",
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Text(
                        text = if (isPasswordVisible) "Скрыть" else "Показать",
                        color = Color(0xFF4FC3F7),
                        fontSize = 12.sp
                    )
                }
            },
            singleLine = true,
            isError = Key.isNotEmpty() && !isKeyValid
        )

        if (Key.isNotEmpty() && !isKeyValid) {
            Text(
                text = "Неверный формат ключа",
                color = Color(0xFFFF5252),
                fontSize = 12.sp
            )
        }

        Text(
            text = "Ключ должен содержать только буквы и цифры",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun UploadButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4FC3F7),
            disabledContainerColor = Color(0xFF2A2A2A),
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        ),
        enabled = isEnabled && !isLoading
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = "Выгрузить",
                tint = if (isEnabled) Color.White else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isLoading) "Выгрузка..." else "Выгрузить на сервер",
                color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}