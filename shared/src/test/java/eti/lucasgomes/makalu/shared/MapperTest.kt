package eti.lucasgomes.makalu.shared

import MakaluConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapperTest {

    @Test
    fun `maps an image id to the images endpoint on the base url`() {
        assertEquals("${MakaluConfig.BASE_URL}images/42", mapImageUrl(42))
    }

    @Test
    fun `maps a null image id to null`() {
        assertNull(mapImageUrl(null))
    }
}
