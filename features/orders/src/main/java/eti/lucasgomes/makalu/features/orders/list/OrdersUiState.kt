package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.mapImageUrl
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal
import kotlin.time.Instant

data class OrdersUiState(
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val orders: List<Item> = emptyList()
) {
    val isEmpty: Boolean
        get() = orders.isEmpty() && !isLoading && generalError == UiText.Empty

    data class Item(
        val id: Long,
        val status: Status,
        val orderNumber: String,
        val storeName: String,
        private val storeImageId: Long?,
        val deliveryAddressLine: String,
        val itemsCount: Int,
        val deliveryFee: BigDecimal,
        val totalPrice: BigDecimal,
        val createdAt: Instant,
        val updatedAt: Instant
    ) {
        val storeImageUrl: String?
            get() = mapImageUrl(storeImageId)

        data class Status(
            val data: OrderStatus,
            val text: UiText
        )
    }
}
