package eti.lucasgomes.features.menuItem.ui.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OptionUiStateTest {

    @Test
    fun `quantity option is not selected when amount is zero`() {
        val option = OptionUiState.Quantity(1, amount = 0)

        assertFalse(option.isSelected)
    }

    @Test
    fun `quantity option is selected when amount is positive`() {
        val option = OptionUiState.Quantity(1, amount = 1)

        assertTrue(option.isSelected)
    }

    @Test
    fun `quantity option total price is additional price times amount`() {
        val option = OptionUiState.Quantity(1, additionalPrice = BigDecimal("2.50"), amount = 3)

        assertEquals(BigDecimal("7.50"), option.totalPrice)
    }

    @Test
    fun `quantity option total price is zero when amount is zero`() {
        val option = OptionUiState.Quantity(1, additionalPrice = BigDecimal("2.50"), amount = 0)

        assertEquals(0, option.totalPrice.signum())
    }

    @Test
    fun `single choice total price is its additional price`() {
        val option = OptionUiState.SingleChoice(1, additionalPrice = BigDecimal("1.75"))

        assertEquals(BigDecimal("1.75"), option.totalPrice)
    }

    @Test
    fun `multiple choice total price is its additional price`() {
        val option = OptionUiState.MultipleChoice(1, additionalPrice = BigDecimal("0.99"))

        assertEquals(BigDecimal("0.99"), option.totalPrice)
    }
}
