package eti.lucasgomes.features.cart.ui

import MakaluConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class CartItemUiStateTest {

    private fun item(imageId: Long?) = CartItemUiState(
        id = 1L,
        imageId = imageId,
        name = "Pizza",
        price = BigDecimal.TEN,
        notes = null,
        configurations = emptyList()
    )

    @Test
    fun `image url is built from the base url and image id`() {
        val imageUrl = item(imageId = 7L).imageUrl

        assertEquals("${MakaluConfig.BASE_URL}images/7", imageUrl)
    }

    @Test
    fun `image url is null without an image id`() {
        val imageUrl = item(imageId = null).imageUrl

        assertNull(imageUrl)
    }
}
