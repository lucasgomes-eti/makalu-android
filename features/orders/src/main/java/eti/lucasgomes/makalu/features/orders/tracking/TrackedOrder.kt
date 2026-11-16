package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

internal data class TrackedOrder(
    val id: Long,
    val orderNumber: String,
    val storeName: String,
    val status: OrderStatus,
    val updatedAt: Instant
)

internal fun OrderSimpleResponse.toTrackedOrder() = TrackedOrder(
    id = id,
    orderNumber = orderNumber,
    storeName = storeName,
    status = status,
    updatedAt = updatedAt
)

internal val OrderStatus.isFinal: Boolean
    get() = this == OrderStatus.FINISHED || this == OrderStatus.CANCELLED

internal val TRACKING_GIVES_UP_AFTER: Duration = 12.hours

internal fun OrderSimpleResponse.isInProgress(now: Instant): Boolean =
    !status.isFinal && now - updatedAt < TRACKING_GIVES_UP_AFTER
