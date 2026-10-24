package eti.lucasgomes.makalu.features.auth.login.model

import eti.lucasgomes.makalu.components.dsl.TextFieldState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.network.MakaluError
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginUiStateTest {

    @Test
    fun `assignFieldErrors maps email and password field errors to plain text`() {
        val state = LoginUiState(
            email = TextFieldState(text = "ana@makalu.com"),
            password = TextFieldState(text = "secret123")
        )

        val result = state.assignFieldErrors(
            listOf(
                MakaluError.FieldError("email", "Unknown email"),
                MakaluError.FieldError("password", "Wrong password")
            )
        ) as LoginUiState

        assertEquals(TextFieldState("ana@makalu.com", UiText.PlainText("Unknown email")), result.email)
        assertEquals(TextFieldState("secret123", UiText.PlainText("Wrong password")), result.password)
    }

    @Test
    fun `assignFieldErrors clears errors for fields missing from the list`() {
        val state = LoginUiState(
            email = TextFieldState(error = UiText.PlainText("old email error")),
            password = TextFieldState(error = UiText.PlainText("old password error"))
        )

        val result = state.assignFieldErrors(
            listOf(MakaluError.FieldError("email", "Unknown email"))
        ) as LoginUiState

        assertEquals(UiText.PlainText("Unknown email"), result.email.error)
        assertEquals(UiText.Empty, result.password.error)
    }

    @Test
    fun `assignFieldErrors ignores unknown fields`() {
        val state = LoginUiState(
            isPasswordVisible = true,
            isLoading = true,
            generalError = UiText.PlainText("general")
        )

        val result = state.assignFieldErrors(listOf(MakaluError.FieldError("name", "Too long")))

        assertEquals(state, result)
    }
}
