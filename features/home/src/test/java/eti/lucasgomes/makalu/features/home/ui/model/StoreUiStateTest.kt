package eti.lucasgomes.makalu.features.home.ui.model

import MakaluConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoreUiStateTest {

    @Test
    fun `data key is the store id as string`() {
        val store = StoreUiState.Data(42, "Store", "Burger", null, null)

        assertEquals("42", store.key)
    }

    @Test
    fun `data logoUrl and coverUrl point to image endpoint`() {
        val store = StoreUiState.Data(1, "Store", "Burger", logoId = 7, coverId = 8)

        assertEquals("${MakaluConfig.BASE_URL}images/7", store.logoUrl)
        assertEquals("${MakaluConfig.BASE_URL}images/8", store.coverUrl)
    }

    @Test
    fun `data logoUrl and coverUrl are null when ids are null`() {
        val store = StoreUiState.Data(1, "Store", "Burger", logoId = null, coverId = null)

        assertNull(store.logoUrl)
        assertNull(store.coverUrl)
    }

    @Test
    fun `loading and no content keys are stable and distinct`() {
        assertEquals(StoreUiState.Loading.key, StoreUiState.Loading.key)
        assertEquals(StoreUiState.NoContent.key, StoreUiState.NoContent.key)
        assertNotEquals(StoreUiState.Loading.key, StoreUiState.NoContent.key)
    }
}
