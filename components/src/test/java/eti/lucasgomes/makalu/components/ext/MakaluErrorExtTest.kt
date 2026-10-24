package eti.lucasgomes.makalu.components.ext

import eti.lucasgomes.makalu.components.FakeErrorsAssignable
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.fieldError
import eti.lucasgomes.makalu.shared.network.MakaluError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MakaluErrorExtTest {

    @Test
    fun `maps each field to its message as plain text`() {
        val errors = listOf(fieldError("email", "Invalid email"), fieldError("name", "Required"))

        val result = errors.withFieldErrorsAsMap { FakeErrorsAssignable(this) }

        assertEquals(
            mapOf(
                "email" to UiText.PlainText("Invalid email"),
                "name" to UiText.PlainText("Required")
            ),
            result.errors
        )
    }

    @Test
    fun `returns the value built by the block`() {
        val expected = FakeErrorsAssignable(emptyMap())

        val result = listOf(fieldError("email", "Invalid")).withFieldErrorsAsMap { expected }

        assertSame(expected, result)
    }

    @Test
    fun `empty field errors give an empty map`() {
        val result = emptyList<MakaluError.FieldError>().withFieldErrorsAsMap { FakeErrorsAssignable(this) }

        assertTrue(result.errors.isEmpty())
    }
}
