package eti.lucasgomes.makalu.features.auth.registration.model

import android.net.Uri
import eti.lucasgomes.makalu.components.dsl.TextFieldState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.network.MakaluError
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationUiStateTest {

    @Test
    fun `isProfileImageLoaded is false without a profile image`() {
        assertFalse(RegistrationUiState().isProfileImageLoaded)
    }

    @Test
    fun `isProfileImageLoaded is true with a profile image`() {
        assertTrue(RegistrationUiState(profileImage = mockk<Uri>()).isProfileImageLoaded)
    }

    @Test
    fun `assignFieldErrors maps name email and password field errors to plain text`() {
        val state = RegistrationUiState(
            name = TextFieldState(text = "Ana"),
            email = TextFieldState(text = "ana@makalu.com"),
            password = TextFieldState(text = "secret123")
        )

        val result = state.assignFieldErrors(
            listOf(
                MakaluError.FieldError("name", "Name too short"),
                MakaluError.FieldError("email", "Email already in use"),
                MakaluError.FieldError("password", "Password too weak")
            )
        )

        assertEquals(TextFieldState("Ana", UiText.PlainText("Name too short")), result.name)
        assertEquals(
            TextFieldState("ana@makalu.com", UiText.PlainText("Email already in use")),
            result.email
        )
        assertEquals(
            TextFieldState("secret123", UiText.PlainText("Password too weak")),
            result.password
        )
    }

    @Test
    fun `assignFieldErrors clears errors for fields missing from the list`() {
        val state = RegistrationUiState(
            name = TextFieldState(error = UiText.PlainText("old name error")),
            email = TextFieldState(error = UiText.PlainText("old email error")),
            password = TextFieldState(error = UiText.PlainText("old password error"))
        )

        val result = state.assignFieldErrors(
            listOf(MakaluError.FieldError("name", "Name too short"))
        )

        assertEquals(UiText.PlainText("Name too short"), result.name.error)
        assertEquals(UiText.Empty, result.email.error)
        assertEquals(UiText.Empty, result.password.error)
    }

    @Test
    fun `assignFieldErrors leaves password confirmation and other state untouched`() {
        val profileImage = mockk<Uri>()
        val confirmation = TextFieldState(text = "secret", error = UiText.PlainText("mismatch"))
        val state = RegistrationUiState(
            profileImage = profileImage,
            passwordConfirmation = confirmation,
            isLoading = true,
            isPasswordVisible = true,
            generalError = UiText.PlainText("general")
        )

        val result = state.assignFieldErrors(
            listOf(MakaluError.FieldError("email", "Email already in use"))
        )

        assertEquals(profileImage, result.profileImage)
        assertEquals(confirmation, result.passwordConfirmation)
        assertTrue(result.isLoading)
        assertTrue(result.isPasswordVisible)
        assertEquals(UiText.PlainText("general"), result.generalError)
    }
}
