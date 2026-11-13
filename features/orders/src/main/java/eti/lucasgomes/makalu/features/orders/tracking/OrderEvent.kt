package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.shared.model.OrderStatus
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A message from an order's realtime channel. It is a hint that something changed and
 * never the source of truth: it can be lost, repeated, or arrive out of order.
 */
internal data class OrderEvent(val orderId: Long, val status: OrderStatus, val updatedAt: Instant)

internal sealed interface ParsedOrderEvent {
    data class Known(val event: OrderEvent) : ParsedOrderEvent

    /** Well-formed, but newer than this app: another version, type or status. */
    data object Unknown : ParsedOrderEvent

    /** Not an order event at all. */
    data object Invalid : ParsedOrderEvent
}

@Serializable
private data class OrderEventMessage(
    val v: Int,
    val type: String,
    val orderId: String? = null,
    val status: String? = null,
    val updatedAt: String? = null
)

private const val SUPPORTED_VERSION = 1
private val KNOWN_TYPES = setOf("order.created", "order.status_changed")
private val eventJson = Json { ignoreUnknownKeys = true }

internal fun parseOrderEvent(payload: String): ParsedOrderEvent {
    val message = try {
        eventJson.decodeFromString<OrderEventMessage>(payload)
    } catch (_: IllegalArgumentException) {
        return ParsedOrderEvent.Invalid
    }
    if (message.v != SUPPORTED_VERSION || message.type !in KNOWN_TYPES) {
        return ParsedOrderEvent.Unknown
    }
    val orderId = message.orderId?.toLongOrNull()
    // Unlike the REST models, an unknown status is not read as PENDING here: guessing
    // would show the customer a status the store never set.
    val status = OrderStatus.entries.firstOrNull { it.name == message.status }
    val updatedAt = message.updatedAt?.let(Instant::parseOrNull)
    if (orderId == null || status == null || updatedAt == null) return ParsedOrderEvent.Unknown
    return ParsedOrderEvent.Known(OrderEvent(orderId, status, updatedAt))
}
