package eti.lucasgomes.features.store.ui.model

import MakaluConfig
import eti.lucasgomes.makalu.components.dsl.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class StoreUiStateTest {

    @Test
    fun `default state is empty and not loading`() {
        val state = StoreUiState(1)

        assertFalse(state.isLoading)
        assertEquals(UiText.Empty, state.generalError)
        assertEquals(UiText.Empty, state.name)
        assertEquals(emptyMap<String, List<MenuItemUiState>>(), state.menuItems)
        assertEquals(BigDecimal.ZERO, state.deliveryFee)
        assertNull(state.coverImageUrl)
    }

    @Test
    fun `coverImageUrl is built from cover image id`() {
        val state = StoreUiState(1, coverImageId = 12)

        assertEquals("${MakaluConfig.BASE_URL}images/12", state.coverImageUrl)
    }
}
