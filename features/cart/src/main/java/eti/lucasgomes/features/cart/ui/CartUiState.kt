package eti.lucasgomes.features.cart.ui

import eti.lucasgomes.makalu.components.dsl.UiText

internal sealed interface CartUiState {
    data object Loading : CartUiState
    data class Error(val generalError: UiText = UiText.Empty) : CartUiState
    data object Empty : CartUiState
    data class Data(val items: List<CartItemUiState> = emptyList()) : CartUiState
}

internal data class CartItemUiState(
    val id: Long,
)