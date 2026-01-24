package eti.lucasgomes.makalu.features.profile

import eti.lucasgomes.makalu.components.dsl.UiText

data class ProfileUiState(
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
)
