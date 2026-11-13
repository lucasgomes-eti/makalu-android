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

/** Nothing follows these: the order is over. */
internal val OrderStatus.isFinal: Boolean
    get() = this == OrderStatus.FINISHED || this == OrderStatus.CANCELLED

/**
 * An order that has not changed for this long is no longer followed.
 *
 * Nothing in the product moves an order from IN_ROUTE to FINISHED yet, so without a
 * limit one forgotten order would keep the service and its notification alive forever.
 */
internal val TRACKING_GIVES_UP_AFTER: Duration = 12.hours

/** Whether the order is still worth a live notification at [now]. */
internal fun OrderSimpleResponse.isInProgress(now: Instant): Boolean =
    !status.isFinal && now - updatedAt < TRACKING_GIVES_UP_AFTER
