package eti.lucasgomes.makalu.shared.model

import eti.lucasgomes.makalu.testing.testJson
import java.math.BigDecimal
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResponseSerializationTest {

    private fun orderJson(storeImageId: String) = """
        {
          "id": 10,
          "status": "IN_ROUTE",
          "order_number": "A-0010",
          "store_name": "Pizzaria",
          "store_image_id": $storeImageId,
          "delivery_address_line": "Rua A, 10",
          "items_count": 3,
          "total": {"delivery_fee": "5.00", "total": "42.90"},
          "created_at": "2026-01-02T10:00:00Z",
          "updated_at": "2026-01-02T10:30:00Z"
        }
    """.trimIndent()

    @Test
    fun `decodes an order summary with snake case fields`() {
        val order = testJson.decodeFromString<OrderSimpleResponse>(orderJson("55"))

        assertEquals(
            OrderSimpleResponse(
                id = 10,
                status = OrderStatus.IN_ROUTE,
                orderNumber = "A-0010",
                storeName = "Pizzaria",
                storeImageId = 55,
                deliveryAddressLine = "Rua A, 10",
                itemsCount = 3,
                total = OrderSimpleResponse.Total(
                    deliveryFee = BigDecimal("5.00"),
                    total = BigDecimal("42.90")
                ),
                createdAt = Instant.parse("2026-01-02T10:00:00Z"),
                updatedAt = Instant.parse("2026-01-02T10:30:00Z")
            ),
            order
        )
    }

    @Test
    fun `decodes an order summary with a null store image`() {
        val order = testJson.decodeFromString<OrderSimpleResponse>(orderJson("null"))

        assertNull(order.storeImageId)
    }

    @Test
    fun `decodes a store with categories and nullable images`() {
        val json = """
            {
              "id": 3,
              "name": "Pizzaria",
              "categories": [{"id": 1, "description": "Pizza"}],
              "logo_image_id": null,
              "cover_image_id": 8,
              "delivery_fee": "7.50"
            }
        """.trimIndent()

        val store = testJson.decodeFromString<StoreResponse>(json)

        assertEquals(
            StoreResponse(
                id = 3,
                name = "Pizzaria",
                categories = listOf(CategoryResponse(1, "Pizza")),
                logoImageId = null,
                coverImageId = 8,
                deliveryFee = BigDecimal("7.50")
            ),
            store
        )
    }
}
