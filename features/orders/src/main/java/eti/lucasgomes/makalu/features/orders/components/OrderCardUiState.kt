package eti.lucasgomes.makalu.features.orders.components

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal

internal data class OrderCardUiState(
    val orderNumber: String,
    val status: OrderStatusUiState,
    val storeImageUrl: String?,
    val storeName: String,
    val deliveryAddressLine: String,
    val itemsCount: Int,
    val deliveryFee: BigDecimal,
    val totalPrice: BigDecimal
)

internal data class OrderStatusUiState(
    val data: OrderStatus,
    val text: UiText
)
