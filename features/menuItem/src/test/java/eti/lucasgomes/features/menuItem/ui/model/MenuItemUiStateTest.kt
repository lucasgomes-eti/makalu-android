package eti.lucasgomes.features.menuItem.ui.model

import MakaluConfig
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuItemUiStateTest {

    private val singleKey = ConfigurationUiState("Size", ConfigurationUiState.Type.SINGLE_CHOICE)
    private val multipleKey =
        ConfigurationUiState("Extras", ConfigurationUiState.Type.MULTIPLE_CHOICE)
    private val quantityKey = ConfigurationUiState("Drinks", ConfigurationUiState.Type.QUANTITY)

    @Test
    fun `total price equals base price when there are no configurations`() {
        val state = MenuItemUiState(id = 1, price = BigDecimal("10.00"))

        assertEquals(BigDecimal("10.00"), state.totalPrice)
    }

    @Test
    fun `total price ignores unselected options`() {
        val state = MenuItemUiState(
            id = 1,
            price = BigDecimal("10.00"),
            configurations = mapOf(
                singleKey to
                    listOf(OptionUiState.SingleChoice(1, additionalPrice = BigDecimal("3.00"))),
                multipleKey to
                    listOf(OptionUiState.MultipleChoice(2, additionalPrice = BigDecimal("1.00")))
            )
        )

        assertEquals(BigDecimal("10.00"), state.totalPrice)
    }

    @Test
    fun `total price adds selected single and multiple choice prices to base price`() {
        val state = MenuItemUiState(
            id = 1,
            price = BigDecimal("10.00"),
            configurations = mapOf(
                singleKey to listOf(
                    OptionUiState.SingleChoice(
                        1,
                        additionalPrice = BigDecimal("1.50"),
                        isSelected = true
                    ),
                    OptionUiState.SingleChoice(2, additionalPrice = BigDecimal("5.00"))
                ),
                multipleKey to listOf(
                    OptionUiState.MultipleChoice(
                        3,
                        additionalPrice = BigDecimal("2.25"),
                        isSelected = true
                    )
                )
            )
        )

        assertEquals(BigDecimal("13.75"), state.totalPrice)
    }

    @Test
    fun `total price multiplies quantity option price by its amount`() {
        val state = MenuItemUiState(
            id = 1,
            price = BigDecimal("10.00"),
            configurations = mapOf(
                quantityKey to
                    listOf(
                        OptionUiState.Quantity(1, additionalPrice = BigDecimal("2.50"), amount = 3)
                    )
            )
        )

        assertEquals(BigDecimal("17.50"), state.totalPrice)
    }

    @Test
    fun `total price ignores quantity options with zero amount`() {
        val state = MenuItemUiState(
            id = 1,
            price = BigDecimal("10.00"),
            configurations = mapOf(
                quantityKey to
                    listOf(
                        OptionUiState.Quantity(1, additionalPrice = BigDecimal("2.50"), amount = 0)
                    )
            )
        )

        assertEquals(BigDecimal("10.00"), state.totalPrice)
    }

    @Test
    fun `is add to cart enabled when loaded and not loading`() {
        val state = MenuItemUiState(id = 1, isLoaded = true, isLoading = false)

        assertTrue(state.isAddToCartEnabled)
    }

    @Test
    fun `is add to cart disabled when not loaded`() {
        val state = MenuItemUiState(id = 1, isLoaded = false, isLoading = false)

        assertFalse(state.isAddToCartEnabled)
    }

    @Test
    fun `is add to cart disabled while loading`() {
        val state = MenuItemUiState(id = 1, isLoaded = true, isLoading = true)

        assertFalse(state.isAddToCartEnabled)
    }

    @Test
    fun `image url is null when image id is null`() {
        val state = MenuItemUiState(id = 1, imageId = null)

        assertNull(state.imageUrl)
    }

    @Test
    fun `image url points to the images endpoint when image id is set`() {
        val state = MenuItemUiState(id = 1, imageId = 9)

        assertEquals("${MakaluConfig.BASE_URL}images/9", state.imageUrl)
    }
}
