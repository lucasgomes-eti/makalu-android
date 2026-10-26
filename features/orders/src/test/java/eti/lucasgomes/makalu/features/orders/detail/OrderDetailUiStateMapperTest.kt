package eti.lucasgomes.makalu.features.orders.detail

import MakaluConfig
import eti.lucasgomes.makalu.components.ItemConfigurationUiState
import eti.lucasgomes.makalu.components.ItemOptionUiState
import eti.lucasgomes.makalu.features.orders.components.toUiStatus
import eti.lucasgomes.makalu.features.orders.configuration
import eti.lucasgomes.makalu.features.orders.option
import eti.lucasgomes.makalu.features.orders.orderDetailedResponse
import eti.lucasgomes.makalu.features.orders.orderItemResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderDetailUiStateMapperTest {

    @Test
    fun `toCardUiState maps the order fields`() {
        val response = orderDetailedResponse(
            status = OrderStatus.FINISHED,
            orderNumber = "0042",
            storeName = "Sushi Naka",
            deliveryAddressLine = "Rua M, 10",
            deliveryFee = BigDecimal("2.50"),
            total = BigDecimal("15.40")
        )

        val card = response.toCardUiState()

        assertEquals("0042", card.orderNumber)
        assertEquals(OrderStatus.FINISHED.toUiStatus(), card.status)
        assertEquals("Sushi Naka", card.storeName)
        assertEquals("Rua M, 10", card.deliveryAddressLine)
        assertEquals(BigDecimal("2.50"), card.deliveryFee)
        assertEquals(BigDecimal("15.40"), card.totalPrice)
    }

    @Test
    fun `toCardUiState counts the items`() {
        val response = orderDetailedResponse(
            items = listOf(
                orderItemResponse(id = 1),
                orderItemResponse(id = 2),
                orderItemResponse(id = 3)
            )
        )

        assertEquals(3, response.toCardUiState().itemsCount)
    }

    @Test
    fun `toCardUiState counts zero items for an empty order`() {
        assertEquals(0, orderDetailedResponse(items = emptyList()).toCardUiState().itemsCount)
    }

    @Test
    fun `toCardUiState builds the store image url`() {
        assertEquals(
            "${MakaluConfig.BASE_URL}images/5",
            orderDetailedResponse(storeImageId = 5).toCardUiState().storeImageUrl
        )
        assertNull(orderDetailedResponse(storeImageId = null).toCardUiState().storeImageUrl)
    }

    @Test
    fun `toUiItems maps item fields`() {
        val response = orderDetailedResponse(
            items = listOf(
                orderItemResponse(
                    id = 8,
                    imageId = 9,
                    name = "Salmon roll",
                    price = BigDecimal("12.90"),
                    notes = "No wasabi"
                )
            )
        )

        val item = response.toUiItems().single()

        assertEquals(8L, item.id)
        assertEquals("Salmon roll", item.name)
        assertEquals("${MakaluConfig.BASE_URL}images/9", item.imageUrl)
        assertEquals(BigDecimal("12.90"), item.price)
        assertEquals("No wasabi", item.notes)
    }

    @Test
    fun `toUiItems leaves image url and notes null when absent`() {
        val response =
            orderDetailedResponse(items = listOf(orderItemResponse(imageId = null, notes = null)))

        val item = response.toUiItems().single()

        assertNull(item.imageUrl)
        assertNull(item.notes)
    }

    @Test
    fun `toUiItems maps configurations and options`() {
        val response = orderDetailedResponse(
            items = listOf(
                orderItemResponse(
                    configurations = listOf(
                        configuration(
                            id = 10,
                            name = "Extras",
                            options = listOf(
                                option(
                                    id = 100,
                                    name = "Cheese",
                                    additionalPrice = BigDecimal("1.50"),
                                    quantity = 2
                                ),
                                option(
                                    id = 101,
                                    name = "Bacon",
                                    additionalPrice = BigDecimal("3.00"),
                                    quantity = 1
                                )
                            )
                        ),
                        configuration(
                            id = 11,
                            name = "Sauce",
                            options = listOf(
                                option(
                                    id = 110,
                                    name = "Teriyaki",
                                    additionalPrice = BigDecimal("0.00"),
                                    quantity = 1
                                )
                            )
                        )
                    )
                )
            )
        )

        val configurations = response.toUiItems().single().configurations

        assertEquals(
            listOf(
                ItemConfigurationUiState(
                    id = 10,
                    name = "Extras",
                    options = listOf(
                        ItemOptionUiState(100, "Cheese", BigDecimal("1.50"), 2),
                        ItemOptionUiState(101, "Bacon", BigDecimal("3.00"), 1)
                    )
                ),
                ItemConfigurationUiState(
                    id = 11,
                    name = "Sauce",
                    options = listOf(ItemOptionUiState(110, "Teriyaki", BigDecimal("0.00"), 1))
                )
            ),
            configurations
        )
    }

    @Test
    fun `toUiItems keeps empty configurations and options`() {
        val response = orderDetailedResponse(
            items = listOf(
                orderItemResponse(
                    id = 1,
                    configurations = listOf(
                        configuration(id = 10, name = "Extras", options = emptyList())
                    )
                ),
                orderItemResponse(id = 2, configurations = emptyList())
            )
        )

        val items = response.toUiItems()

        assertEquals(
            listOf(ItemConfigurationUiState(10, "Extras", emptyList())),
            items[0].configurations
        )
        assertTrue(items[1].configurations.isEmpty())
    }

    @Test
    fun `toUiItems of an order without items is empty`() {
        assertTrue(orderDetailedResponse(items = emptyList()).toUiItems().isEmpty())
    }
}
