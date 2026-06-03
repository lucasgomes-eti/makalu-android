package eti.lucasgomes.makalu.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    PENDING,
    ACCEPTED,
    CANCELLED,
    IN_ROUTE,
    FINISHED,
}
