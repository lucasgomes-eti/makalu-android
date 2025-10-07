package eti.lucasgomes.makalu.features.auth.login.model

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
)
