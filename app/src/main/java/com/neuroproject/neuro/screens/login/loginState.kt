package com.neuroproject.neuro.screens.login

sealed class LoginState {
    object Disconnected : LoginState()
    object Connecting : LoginState()
    object Connected : LoginState()
    object Error : LoginState()
}