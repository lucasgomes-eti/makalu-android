package eti.lucasgomes.features.store.ui.model

import MakaluConfig
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MenuItemUiStateTest {

    @Test
    fun `imageUrl is built from image id`() {
        val item = MenuItemUiState(1, imageId = 4)

        assertEquals("${MakaluConfig.BASE_URL}images/4", item.imageUrl)
    }

    @Test
    fun `imageUrl is null without image id`() {
        val item = MenuItemUiState(1)

        assertNull(item.imageUrl)
    }

    @Test
    fun `defaults are empty values`() {
        val item = MenuItemUiState(1)

        assertEquals("", item.category)
        assertEquals("", item.name)
        assertEquals(BigDecimal.ZERO, item.price)
    }
}
