// LoginScreenViewModel.kt
// LoginScreenViewModel.kt
package com.neuroproject.neuro.screens.login

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Disconnected)
    val loginState = _loginState.asStateFlow()

    val individualNumber = mutableStateOf("")
    val userName = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)

    private val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    init {
        loadSavedData()
    }

    fun onIndividualNumberChange(number: String) {
        individualNumber.value = number
        errorMessage.value = null
    }

    fun onUserNameChange(name: String) {
        userName.value = name
        errorMessage.value = null
    }

    fun login() {
        val number = individualNumber.value.trim()
        val name = userName.value.trim()

        if (!isFormValid(number, name)) {
            return
        }

        _loginState.value = LoginState.Connecting
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val success = performLogin(number)
                if (success) {
                    saveUserData(number, name)
                    _loginState.value = LoginState.Connected
                } else {
                    _loginState.value = LoginState.Error
                    errorMessage.value = "Неверный индивидуальный номер"
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error
                errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Disconnected
        }
    }

    private fun isFormValid(number: String, name: String): Boolean {
        if (number.isBlank()) {
            errorMessage.value = "Введите индивидуальный номер"
            return false
        }

        if (number.length < 3) {
            errorMessage.value = "Номер должен содержать минимум 3 символа"
            return false
        }

        if (!number.matches(Regex("^[a-zA-Z0-9]+$"))) {
            errorMessage.value = "Номер может содержать только буквы и цифры"
            return false
        }

        if (name.isNotEmpty() && name.length < 2) {
            errorMessage.value = "Имя должно содержать минимум 2 символа"
            return false
        }

        return true
    }

    private suspend fun performLogin(individualNumber: String): Boolean {
        return individualNumber.length >= 3
    }

    private fun saveUserData(number: String, name: String) {
        val editor = sharedPreferences.edit()
        editor.putString("saved_individual_number", number)

        // Сохраняем имя только если оно было введено
        if (name.isNotEmpty()) {
            editor.putString("saved_user_name", name)
        }
        editor.apply()
    }

    private fun loadSavedData() {
        val savedNumber = sharedPreferences.getString("saved_individual_number", "")
        val savedName = sharedPreferences.getString("saved_user_name", "")

        if (!savedNumber.isNullOrEmpty()) {
            individualNumber.value = savedNumber
        }

        if (!savedName.isNullOrEmpty()) {
            userName.value = savedName
        }
    }

    fun clearSavedData() {
        sharedPreferences.edit().apply {
            remove("saved_individual_number")
            remove("saved_user_name")
        }.apply()

        individualNumber.value = ""
        userName.value = ""
        _loginState.value = LoginState.Disconnected
    }


    fun hasSavedData(): Boolean {
        return sharedPreferences.contains("saved_individual_number")
    }
}