package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.components.OrderCardUiState
import kotlin.time.Instant

internal data class OrdersUiState(
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val orders: List<Item> = emptyList()
) {
    val isEmpty: Boolean
        get() = orders.isEmpty() && !isLoading && generalError == UiText.Empty

    data class Item(
        val id: Long,
        val card: OrderCardUiState,
        val createdAt: Instant,
        val updatedAt: Instant
    )
}
