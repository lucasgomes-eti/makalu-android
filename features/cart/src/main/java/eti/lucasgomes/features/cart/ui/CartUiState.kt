package eti.lucasgomes.features.cart.ui

import eti.lucasgomes.makalu.components.dsl.UiText

internal data class CartUiState(
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val items: List<CartItemUiState> = emptyList()
)

internal data class CartItemUiState(
    val id: Long,
)