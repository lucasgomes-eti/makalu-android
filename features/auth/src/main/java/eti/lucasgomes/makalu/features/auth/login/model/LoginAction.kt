package eti.lucasgomes.makalu.features.auth.login.model

sealed interface LoginAction {
    data class EmailChanged(val text: String) : LoginAction
    data class PasswordChanged(val text: String) : LoginAction
    data object ShowPasswordClicked : LoginAction
    data object LoginClicked : LoginAction
    data object RegistrationClicked : LoginAction
}