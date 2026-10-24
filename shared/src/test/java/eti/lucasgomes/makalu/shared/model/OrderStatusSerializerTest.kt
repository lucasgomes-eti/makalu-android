package eti.lucasgomes.makalu.shared.model

import eti.lucasgomes.makalu.testing.testJson
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderStatusSerializerTest {

    @Test
    fun `decodes every known status by name`() {
        OrderStatus.entries.forEach { status ->
            assertEquals(status, testJson.decodeFromString<OrderStatus>("\"${status.name}\""))
        }
    }

    @Test
    fun `encodes a status as its name`() {
        assertEquals("\"IN_ROUTE\"", testJson.encodeToString(OrderStatus.IN_ROUTE))
    }

    @Test
    fun `decodes an unknown status as pending`() {
        assertEquals(OrderStatus.PENDING, testJson.decodeFromString<OrderStatus>("\"DELIVERED\""))
    }

    @Test
    fun `decodes a status with different casing as pending`() {
        assertEquals(OrderStatus.PENDING, testJson.decodeFromString<OrderStatus>("\"accepted\""))
    }
}
