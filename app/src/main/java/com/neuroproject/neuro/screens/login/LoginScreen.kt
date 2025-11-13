// LoginScreen.kt
package com.neuroproject.neuro.screens.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vm: LoginScreenViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = { },
    onBackPressed: () -> Unit = {}
) {
    val loginState by vm.loginState.collectAsState()
    val individualNumber = vm.individualNumber.value
    val userName = vm.userName.value
    val errorMessage = vm.errorMessage.value

    // Автоматически переходим при успешном входе
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Connected) {
            onLoginSuccess()
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
                    "Вход в систему",
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

                // Статус подключения
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (loginState) {
                        is LoginState.Connecting -> {
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
                                    "Выполняется вход...",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        is LoginState.Connected -> {
                            Text(
                                "Вход выполнен успешно!",
                                color = Color.Green,
                                fontSize = 16.sp
                            )
                        }
                        is LoginState.Error -> {
                            Text(
                                "Ошибка входа",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                        }
                        else -> {
                            Text(
                                "Введите данные для входа",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Поле ввода номера
                IndividualNumberField(
                    value = individualNumber,
                    onValueChange = vm::onIndividualNumberChange,
                    placeholder = "Индивидуальный номер *",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Поле ввода имени (необязательное)
                UserNameField(
                    value = userName,
                    onValueChange = vm::onUserNameChange,
                    placeholder = "Ваше имя (необязательно)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Сообщение об ошибке
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }

                // Информация о сохраненных данных
                if (vm.hasSavedData()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Сохраненные данные:",
                            style = TextStyle(
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    Text(
                        text = "Данные будут сохранены после входа",
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Кнопка входа
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = vm::login,
                        enabled = individualNumber.isNotBlank() && loginState !is LoginState.Connecting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        when {
                            loginState is LoginState.Connecting -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .aspectRatio(1f),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                            loginState is LoginState.Connected -> {
                                Text(
                                    "✓ Успешно",
                                    color = Color.Green,
                                    fontSize = 18.sp
                                )
                            }
                            else -> {
                                Text(
                                    "Войти",
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                // Кнопка очистки сохраненных данных
                if (vm.hasSavedData() && loginState !is LoginState.Connecting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = vm::clearSavedData,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                "Очистить сохраненные данные",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IndividualNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Номер",
                tint = Color.White
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    autoCorrect = false
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun UserNameField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = "Имя",
                tint = Color.White
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    autoCorrect = false
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    LoginScreen(vm = hiltViewModel())
}