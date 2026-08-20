package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.features.orders.components.OrderCardUiState
import eti.lucasgomes.makalu.features.orders.components.toUiStatus
import eti.lucasgomes.makalu.shared.mapImageUrl
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse

internal fun List<OrderSimpleResponse>.toUiItems(): List<OrdersUiState.Item> =
    map { it.toUiItem() }

internal fun OrderSimpleResponse.toUiItem(): OrdersUiState.Item = OrdersUiState.Item(
    id = id,
    card = OrderCardUiState(
        orderNumber = orderNumber,
        status = status.toUiStatus(),
        storeImageUrl = mapImageUrl(storeImageId),
        storeName = storeName,
        deliveryAddressLine = deliveryAddressLine,
        itemsCount = itemsCount,
        deliveryFee = total.deliveryFee,
        totalPrice = total.total
    ),
    createdAt = createdAt,
    updatedAt = updatedAt
)
