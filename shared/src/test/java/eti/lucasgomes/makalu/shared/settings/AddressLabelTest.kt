package eti.lucasgomes.makalu.shared.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AddressLabelTest {

    @Test
    fun `joins street and number with a comma`() {
        assertEquals("Rua A, 10", addressLabel("Rua A", "10"))
    }

    @Test
    fun `returns only the street when the number is null`() {
        assertEquals("Rua A", addressLabel("Rua A", null))
    }

    @Test
    fun `returns only the street when the number is empty`() {
        assertEquals("Rua A", addressLabel("Rua A", ""))
    }

    @Test
    fun `returns only the street when the number is blank`() {
        assertEquals("Rua A", addressLabel("Rua A", "  "))
    }
}
