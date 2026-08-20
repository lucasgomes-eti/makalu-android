package eti.lucasgomes.makalu.features.orders.components

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.shared.model.OrderStatus

internal fun OrderStatus.toUiStatus(): OrderStatusUiState = OrderStatusUiState(
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
