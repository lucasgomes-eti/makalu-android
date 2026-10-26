package eti.lucasgomes.makalu.features.orders.list

import MakaluConfig
import eti.lucasgomes.makalu.features.orders.CREATED_AT
import eti.lucasgomes.makalu.features.orders.UPDATED_AT
import eti.lucasgomes.makalu.features.orders.components.toUiStatus
import eti.lucasgomes.makalu.features.orders.orderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdersUiStateMapperTest {

    @Test
    fun `toUiItem maps id and timestamps`() {
        val item = orderSimpleResponse(id = 3).toUiItem()

        assertEquals(3L, item.id)
        assertEquals(CREATED_AT, item.createdAt)
        assertEquals(UPDATED_AT, item.updatedAt)
    }

    @Test
    fun `toUiItem maps the card fields`() {
        val response = orderSimpleResponse(
            status = OrderStatus.IN_ROUTE,
            orderNumber = "0099",
            storeName = "Pizza Place",
            deliveryAddressLine = "Av. Paulista, 1000",
            itemsCount = 4,
            deliveryFee = BigDecimal("3.00"),
            total = BigDecimal("50.00")
        )

        val card = response.toUiItem().card

        assertEquals("0099", card.orderNumber)
        assertEquals(OrderStatus.IN_ROUTE.toUiStatus(), card.status)
        assertEquals("Pizza Place", card.storeName)
        assertEquals("Av. Paulista, 1000", card.deliveryAddressLine)
        assertEquals(4, card.itemsCount)
        assertEquals(BigDecimal("3.00"), card.deliveryFee)
        assertEquals(BigDecimal("50.00"), card.totalPrice)
    }

    @Test
    fun `toUiItem builds the store image url from the image id`() {
        val card = orderSimpleResponse(storeImageId = 7).toUiItem().card

        assertEquals("${MakaluConfig.BASE_URL}images/7", card.storeImageUrl)
    }

    @Test
    fun `toUiItem leaves the store image url null without an image id`() {
        val card = orderSimpleResponse(storeImageId = null).toUiItem().card

        assertNull(card.storeImageUrl)
    }

    @Test
    fun `toUiItem keeps the money values untouched`() {
        val card = orderSimpleResponse(
            deliveryFee = BigDecimal("2.50"),
            total = BigDecimal("24.00")
        ).toUiItem().card

        assertEquals(BigDecimal("2.50"), card.deliveryFee)
        assertEquals(2, card.deliveryFee.scale())
        assertEquals(BigDecimal("24.00"), card.totalPrice)
        assertEquals(2, card.totalPrice.scale())
    }

    @Test
    fun `toUiItems preserves order and size`() {
        val responses =
            listOf(
                orderSimpleResponse(id = 3),
                orderSimpleResponse(id = 1),
                orderSimpleResponse(id = 2)
            )

        val items = responses.toUiItems()

        assertEquals(listOf(3L, 1L, 2L), items.map { it.id })
    }

    @Test
    fun `toUiItems of an empty list is empty`() {
        assertTrue(emptyList<OrderSimpleResponse>().toUiItems().isEmpty())
    }
}
