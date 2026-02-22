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
        onMobileIdChanged = vm::onMobileIdChanged,
        onExpeditionIdChanged = vm::onExpeditionIdChanged,
        onUploadClicked = vm::onUploadClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsState,
    onBackClick: () -> Unit,
    onMobileIdChanged: (String) -> Unit,
    onExpeditionIdChanged: (String) -> Unit,
    onUploadClicked: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Профиль",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            InvisibleProgressBar(
                isVisible = state.isUploading,
                progress = state.uploadProgress
            )


            MobileIdField(
                mobileId = state.mobileId,
                onValueChange = onMobileIdChanged
            )

            ExpeditionIdField(
                expeditionId = state.expeditionId,
                onValueChange = onExpeditionIdChanged
            )

            Spacer(modifier = Modifier.height(8.dp))

            UploadButton(
                isLoading = state.isUploading,
                isEnabled = !state.isUploading,
                onClick = onUploadClicked
            )

            if (state.isUploading) {
                UploadProgressStatus(
                    progress = state.uploadProgress
                )
            }

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
            .alpha(if (isVisible) 1f else 0f)
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
private fun MobileIdField(
    mobileId: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "ID мобильного пользователя",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = mobileId,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF4FC3F7),
                unfocusedBorderColor = Color(0xFF2A2A2A),
                focusedLabelColor = Color(0xFF4FC3F7),
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                containerColor = Color(0xFF1A1A1A),
                cursorColor = Color(0xFF4FC3F7)
            ),
            placeholder = {
                Text(
                    text = "Введите ID мобильного пользователя",
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )

        Text(
            text = "Идентификатор пользователя в мобильном приложении",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpeditionIdField(
    expeditionId: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "ID экспедиции",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = expeditionId,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF4FC3F7),
                unfocusedBorderColor = Color(0xFF2A2A2A),
                focusedLabelColor = Color(0xFF4FC3F7),
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                containerColor = Color(0xFF1A1A1A),
                cursorColor = Color(0xFF4FC3F7)
            ),
            placeholder = {
                Text(
                    text = "Введите ID экспедиции",
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )

        Text(
            text = "Идентификатор экспедиции/исследования",
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