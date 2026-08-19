package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.mapImageUrl
import eti.lucasgomes.makalu.shared.model.OrderStatus

data class OrdersUiState(
    val orders: List<Item>
) {
    data class Item(
        val id: Long,
        val status: Status,
        val orderNumber: String,
        val storeName: String,
        private val storeImageId: Long?,
        val deliveryAddressLine: String,
        val itemsCount: Int,
        val deliveryFee: String,
        val totalPrice: String,
        val createdAt: String,
        val updatedAt: String
    ) {
        val storeImageUrl: String?
            get() = mapImageUrl(storeImageId)

        data class Status(
            val data: OrderStatus,
            val text: UiText
        )
    }
}
