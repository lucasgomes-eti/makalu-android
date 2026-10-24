package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.orderSimpleResponse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdersUiStateTest {

    @Test
    fun `default state is empty`() {
        assertTrue(OrdersUiState().isEmpty)
    }

    @Test
    fun `is not empty while loading`() {
        assertFalse(OrdersUiState(isLoading = true).isEmpty)
    }

    @Test
    fun `is not empty when there is a general error`() {
        assertFalse(OrdersUiState(generalError = UiText.PlainText("x")).isEmpty)
    }

    @Test
    fun `is not empty when there are orders`() {
        assertFalse(OrdersUiState(orders = listOf(orderSimpleResponse().toUiItem())).isEmpty)
    }
}
