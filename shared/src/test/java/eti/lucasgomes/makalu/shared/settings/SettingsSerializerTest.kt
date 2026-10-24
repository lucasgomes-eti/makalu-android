package eti.lucasgomes.makalu.shared.settings

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SettingsSerializerTest {

    private suspend fun read(content: String): Settings =
        SettingsSerializer.readFrom(ByteArrayInputStream(content.encodeToByteArray()))

    @Test
    fun `default value is empty settings`() {
        assertEquals(Settings(), SettingsSerializer.defaultValue)
    }

    @Test
    fun `reads all persisted fields`() = runTest {
        val settings = read(
            """{"accessToken":"a","refreshToken":"r","addressId":7,"addressName":"Home"}"""
        )

        assertEquals(Settings("a", "r", 7, "Home"), settings)
    }

    @Test
    fun `reads missing fields as null`() = runTest {
        assertEquals(Settings(), read("{}"))
    }

    @Test
    fun `write then read round trips the settings`() = runTest {
        val settings = Settings("a", "r", 7, "Home")
        val output = ByteArrayOutputStream()

        SettingsSerializer.writeTo(settings, output)

        assertEquals(settings, SettingsSerializer.readFrom(ByteArrayInputStream(output.toByteArray())))
    }

    @Test
    fun `throws a corruption exception for malformed json`() = runTest {
        try {
            read("not json")
            fail("expected CorruptionException")
        } catch (e: CorruptionException) {
            assertTrue(e.cause is SerializationException)
        }
    }

    @Test
    fun `throws a corruption exception for an empty file`() = runTest {
        try {
            read("")
            fail("expected CorruptionException")
        } catch (_: CorruptionException) {
        }
    }
}
