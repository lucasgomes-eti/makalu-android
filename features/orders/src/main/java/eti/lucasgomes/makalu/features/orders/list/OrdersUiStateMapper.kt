package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus

internal fun List<OrderSimpleResponse>.toUiItems(): List<OrdersUiState.Item> =
    map { it.toUiItem() }

internal fun OrderSimpleResponse.toUiItem(): OrdersUiState.Item = OrdersUiState.Item(
    id = id,
    status = status.toUiStatus(),
    orderNumber = orderNumber,
    storeName = storeName,
    storeImageId = storeImageId,
    deliveryAddressLine = deliveryAddressLine,
    itemsCount = itemsCount,
    deliveryFee = total.deliveryFee,
    totalPrice = total.total,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun OrderStatus.toUiStatus(): OrdersUiState.Item.Status = OrdersUiState.Item.Status(
    data = this,
    text = UiText.StringResource(
        when (this) {
            OrderStatus.PENDING -> R.string.order_status_pending
            OrderStatus.ACCEPTED -> R.string.order_status_accepted
            OrderStatus.CANCELLED -> R.string.order_status_cancelled
            OrderStatus.IN_ROUTE -> R.string.order_status_in_route
            OrderStatus.FINISHED -> R.string.order_status_finished
        }
    )
)
