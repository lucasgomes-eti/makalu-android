package eti.lucasgomes.makalu.features.orders.model

import eti.lucasgomes.makalu.shared.model.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderStatusRequest(val status: OrderStatus)
