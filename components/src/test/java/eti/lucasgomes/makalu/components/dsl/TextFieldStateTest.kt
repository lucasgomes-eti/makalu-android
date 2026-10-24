package eti.lucasgomes.makalu.components.dsl

import eti.lucasgomes.makalu.components.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextFieldStateTest {

    @Test
    fun `default state is empty enabled and without error`() {
        val state = TextFieldState()

        assertEquals("", state.text)
        assertEquals(UiText.Empty, state.error)
        assertTrue(state.isEnabled)
        assertFalse(state.hasError)
    }

    @Test
    fun `hasError is true with a plain text error`() {
        val state = TextFieldState(error = UiText.PlainText("Required"))

        assertTrue(state.hasError)
    }

    @Test
    fun `hasError is true with a string resource error`() {
        val state = TextFieldState(error = UiText.StringResource(R.string.image_load_error))

        assertTrue(state.hasError)
    }

    @Test
    fun `clearing the error resets hasError`() {
        val state = TextFieldState(text = "x", error = UiText.PlainText("Required"))

        val cleared = state.copy(error = UiText.Empty)

        assertFalse(cleared.hasError)
    }
}
