package eti.lucasgomes.makalu.shared.serializer

import eti.lucasgomes.makalu.testing.testJson
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class BigDecimalSerializerTest {

    @Test
    fun `decodes a string keeping its scale`() {
        val value = Json.decodeFromString(BigDecimalSerializer, "\"12.50\"")

        assertEquals(BigDecimal("12.50"), value)
    }

    @Test
    fun `decodes an unquoted number with the lenient production json`() {
        val value = testJson.decodeFromString(BigDecimalSerializer, "12.50")

        assertEquals(BigDecimal("12.50"), value)
    }

    @Test
    fun `encodes as a plain string`() {
        val json = Json.encodeToString(BigDecimalSerializer, BigDecimal("12.50"))

        assertEquals("\"12.50\"", json)
    }

    @Test
    fun `encodes scientific notation values in plain form`() {
        val json = Json.encodeToString(BigDecimalSerializer, BigDecimal("1E+3"))

        assertEquals("\"1000\"", json)
    }

    @Test
    fun `throws when the value is not a number`() {
        assertThrows(NumberFormatException::class.java) {
            Json.decodeFromString(BigDecimalSerializer, "\"abc\"")
        }
    }

    @Test
    fun `descriptor is a string primitive named BigDecimal`() {
        assertEquals("BigDecimal", BigDecimalSerializer.descriptor.serialName)
        assertEquals(PrimitiveKind.STRING, BigDecimalSerializer.descriptor.kind)
    }
}
