package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.shared.model.OrderStatus
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderEventTest {

    private fun message(
        v: Int = 1,
        type: String = "order.status_changed",
        orderId: String = "42",
        status: String = "IN_ROUTE",
        updatedAt: String = "2026-11-13T18:04:11.123456Z"
    ) = """{"v":$v,"type":"$type","orderId":"$orderId","storeId":"7",""" +
        """"status":"$status","updatedAt":"$updatedAt"}"""

    @Test
    fun `reads a status change`() {
        assertEquals(
            ParsedOrderEvent.Known(
                OrderEvent(
                    42,
                    OrderStatus.IN_ROUTE,
                    Instant.parse("2026-11-13T18:04:11.123456Z")
                )
            ),
            parseOrderEvent(message())
        )
    }

    @Test
    fun `reads a creation event`() {
        val parsed = parseOrderEvent(message(type = "order.created", status = "PENDING"))

        assertEquals(OrderStatus.PENDING, (parsed as ParsedOrderEvent.Known).event.status)
    }

    @Test
    fun `ignores fields it does not know`() {
        val payload = message().dropLast(1) + ""","extra":{"a":1}}"""

        assertEquals(42L, (parseOrderEvent(payload) as ParsedOrderEvent.Known).event.orderId)
    }

    @Test
    fun `a newer version is unknown`() {
        assertEquals(ParsedOrderEvent.Unknown, parseOrderEvent(message(v = 2)))
    }

    @Test
    fun `an unknown type is unknown`() {
        assertEquals(ParsedOrderEvent.Unknown, parseOrderEvent(message(type = "order.refunded")))
    }

    @Test
    fun `an unknown status is unknown rather than read as pending`() {
        assertEquals(ParsedOrderEvent.Unknown, parseOrderEvent(message(status = "ON_HOLD")))
    }

    @Test
    fun `an unreadable id or timestamp is unknown`() {
        assertEquals(ParsedOrderEvent.Unknown, parseOrderEvent(message(orderId = "abc")))
        assertEquals(ParsedOrderEvent.Unknown, parseOrderEvent(message(updatedAt = "yesterday")))
    }

    @Test
    fun `anything that is not an order event is invalid`() {
        assertEquals(ParsedOrderEvent.Invalid, parseOrderEvent("not json"))
        assertEquals(ParsedOrderEvent.Invalid, parseOrderEvent("""{"status":401}"""))
        assertEquals(ParsedOrderEvent.Invalid, parseOrderEvent("[]"))
    }
}
