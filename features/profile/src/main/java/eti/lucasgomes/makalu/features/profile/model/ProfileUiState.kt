package eti.lucasgomes.makalu.features.profile.model

import eti.lucasgomes.makalu.components.dsl.UiText

data class ProfileUiState(
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isLogoutDialogVisible: Boolean = false
)