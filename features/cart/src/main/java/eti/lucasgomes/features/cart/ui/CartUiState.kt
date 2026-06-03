package eti.lucasgomes.features.cart.ui

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.mapImageUrl
import java.math.BigDecimal

internal sealed interface CartUiState {
    data object Loading : CartUiState
    data class Error(val generalError: UiText = UiText.Empty) : CartUiState
    data object Empty : CartUiState
    data class Data(
        val items: List<CartItemUiState> = emptyList(),
        val address: UiText = UiText.Empty,
        val deliveryFee: BigDecimal = BigDecimal.ZERO,
        val totalPrice: BigDecimal = BigDecimal.ZERO,
        val isMakingOrder: Boolean = false
    ) : CartUiState
}

internal data class CartItemUiState(
    val id: Long,
    private val imageId: Long?,
    val name: String,
    val price: BigDecimal,
    val notes: String?,
    val isRemoving: Boolean = false,
    val configurations: List<Configuration>
) {
    val imageUrl: String?
        get() = mapImageUrl(imageId)

    data class Configuration(
        val configurationId: Long,
        val name: String,
        val options: List<Option>
    ) {
        data class Option(
            val id: Long,
            val name: String,
            val additionalPrice: BigDecimal,
            val quantity: Int
        )
    }
}