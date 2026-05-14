package eti.lucasgomes.features.cart.ui

internal data class CartUiState(
    val items: List<CartItemUiState> = emptyList()
)

internal data class CartItemUiState(
    val id: Long,
)