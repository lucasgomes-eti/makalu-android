package eti.lucasgomes.makalu.features.orders.detail

import eti.lucasgomes.makalu.components.ItemConfigurationUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.components.OrderCardUiState
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal

internal data class OrderDetailUiState(
    val id: Long,
    val isLoading: Boolean = false,
    val isConfirmingDelivery: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val card: OrderCardUiState? = null,
    val items: List<Item> = emptyList()
) {
    val canCustomerConfirmDelivery: Boolean
        get() = card?.status?.data == OrderStatus.IN_ROUTE

    data class Item(
        val id: Long,
        val name: String,
        val imageUrl: String?,
        val price: BigDecimal,
        val notes: String?,
        val configurations: List<ItemConfigurationUiState>
    )
}
