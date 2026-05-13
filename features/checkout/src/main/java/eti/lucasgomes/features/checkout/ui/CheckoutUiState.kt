package eti.lucasgomes.features.checkout.ui

internal data class CheckoutUiState(
    val items: List<CartItemUiState> = emptyList()
)

internal data class CartItemUiState(
    val id: Long,
)