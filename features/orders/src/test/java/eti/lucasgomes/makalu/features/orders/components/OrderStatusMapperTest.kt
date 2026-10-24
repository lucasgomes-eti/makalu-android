package eti.lucasgomes.makalu.features.orders.components

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.shared.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderStatusMapperTest {

    @Test
    fun `pending maps to the pending string`() {
        assertEquals(
            OrderStatusUiState(OrderStatus.PENDING, UiText.StringResource(R.string.order_status_pending)),
            OrderStatus.PENDING.toUiStatus()
        )
    }

    @Test
    fun `accepted maps to the accepted string`() {
        assertEquals(
            OrderStatusUiState(OrderStatus.ACCEPTED, UiText.StringResource(R.string.order_status_accepted)),
            OrderStatus.ACCEPTED.toUiStatus()
        )
    }

    @Test
    fun `cancelled maps to the cancelled string`() {
        assertEquals(
            OrderStatusUiState(OrderStatus.CANCELLED, UiText.StringResource(R.string.order_status_cancelled)),
            OrderStatus.CANCELLED.toUiStatus()
        )
    }

    @Test
    fun `in route maps to the in route string`() {
        assertEquals(
            OrderStatusUiState(OrderStatus.IN_ROUTE, UiText.StringResource(R.string.order_status_in_route)),
            OrderStatus.IN_ROUTE.toUiStatus()
        )
    }

    @Test
    fun `finished maps to the finished string`() {
        assertEquals(
            OrderStatusUiState(OrderStatus.FINISHED, UiText.StringResource(R.string.order_status_finished)),
            OrderStatus.FINISHED.toUiStatus()
        )
    }

    @Test
    fun `every status keeps itself as data`() {
        val mapped = OrderStatus.entries.map { it.toUiStatus() }

        assertEquals(OrderStatus.entries, mapped.map { it.data })
        assertEquals(OrderStatus.entries.size, mapped.map { it.text }.toSet().size)
    }
}
