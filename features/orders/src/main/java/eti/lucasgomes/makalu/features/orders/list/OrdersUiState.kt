package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.shared.mapImageUrl

data class OrdersUiState(
    val orders: List<OrderSimpleUiState>
)

data class OrderSimpleUiState(
    val id: Long,
    val status: String,
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
}