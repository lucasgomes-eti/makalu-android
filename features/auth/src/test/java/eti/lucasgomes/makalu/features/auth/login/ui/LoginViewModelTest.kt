package eti.lucasgomes.makalu.features.auth.login.ui

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.auth.AuthClient
import eti.lucasgomes.makalu.features.auth.R
import eti.lucasgomes.makalu.features.auth.login.model.LoginAction
import eti.lucasgomes.makalu.features.auth.login.model.LoginRequest
import eti.lucasgomes.makalu.features.auth.login.model.LoginUiState
import eti.lucasgomes.makalu.features.auth.makaluError
import eti.lucasgomes.makalu.shared.MIN_PASSWORD_LENGTH
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.network.TokenPairResponse
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authClient: AuthClient = mockk()
    private val navigator: Navigator = mockk(relaxed = true)
    private val viewModel = LoginViewModel(authClient, navigator)

    private val tokenPair = TokenPairResponse("access", "refresh")
    private val homeNavOptions = NavOptions(
        popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true)
    )

    private fun typeCredentials(email: String = VALID_EMAIL, password: String = VALID_PASSWORD) {
        viewModel.onAction(LoginAction.EmailChanged(email))
        viewModel.onAction(LoginAction.PasswordChanged(password))
    }

    private val state get() = viewModel.uiState.value

    @Test
    fun `initial state is empty and idle`() {
        assertEquals(LoginUiState(), state)
    }

    @Test
    fun `email changed updates text and clears email error`() {
        typeCredentials(email = "")
        viewModel.onAction(LoginAction.LoginClicked)

        viewModel.onAction(LoginAction.EmailChanged("a@b.com"))

        assertEquals("a@b.com", state.email.text)
        assertEquals(UiText.Empty, state.email.error)
    }

    @Test
    fun `password changed updates text and clears password error`() {
        typeCredentials(password = "")
        viewModel.onAction(LoginAction.LoginClicked)

        viewModel.onAction(LoginAction.PasswordChanged("abc12345"))

        assertEquals("abc12345", state.password.text)
        assertEquals(UiText.Empty, state.password.error)
    }

    @Test
    fun `show password clicked toggles visibility`() {
        viewModel.onAction(LoginAction.ShowPasswordClicked)
        assertTrue(state.isPasswordVisible)

        viewModel.onAction(LoginAction.ShowPasswordClicked)
        assertFalse(state.isPasswordVisible)
    }

    @Test
    fun `registration clicked navigates to registration screen`() {
        viewModel.onAction(LoginAction.RegistrationClicked)

        coVerify(exactly = 1) { navigator.navigate(Destination.Screen.Registration) }
    }

    @Test
    fun `dismiss error clears general error`() = runTest {
        coEvery { authClient.login(any()) } returns Resource.Error(makaluError())
        typeCredentials()
        viewModel.onAction(LoginAction.LoginClicked)

        viewModel.onAction(LoginAction.DismissError)

        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `login with blank email sets email required error`() {
        typeCredentials(email = "  ")

        viewModel.onAction(LoginAction.LoginClicked)

        assertEquals(UiText.StringResource(R.string.email_is_required), state.email.error)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authClient.login(any()) }
    }

    @Test
    fun `login with malformed email sets invalid format error`() {
        typeCredentials(email = "not-an-email")

        viewModel.onAction(LoginAction.LoginClicked)

        assertEquals(
            UiText.StringResource(R.string.email_must_have_valid_format),
            state.email.error
        )
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authClient.login(any()) }
    }

    @Test
    fun `login with blank password sets password required error`() {
        typeCredentials(password = "")

        viewModel.onAction(LoginAction.LoginClicked)

        assertEquals(UiText.StringResource(R.string.password_is_required), state.password.error)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authClient.login(any()) }
    }

    @Test
    fun `login with short password sets minimum length error`() {
        typeCredentials(password = "abc123")

        viewModel.onAction(LoginAction.LoginClicked)

        assertEquals(
            UiText.StringResource(
                R.string.password_must_have_at_least_characters,
                listOf(MIN_PASSWORD_LENGTH)
            ),
            state.password.error
        )
        coVerify(exactly = 0) { authClient.login(any()) }
    }

    @Test
    fun `login with password without digits sets letters and numbers error`() {
        typeCredentials(password = "abcdefgh")

        viewModel.onAction(LoginAction.LoginClicked)

        assertEquals(
            UiText.StringResource(R.string.password_must_have_letters_and_numbers),
            state.password.error
        )
        coVerify(exactly = 0) { authClient.login(any()) }
    }

    @Test
    fun `login with invalid email and password reports both errors`() {
        viewModel.onAction(LoginAction.LoginClicked)

        assertEquals(UiText.StringResource(R.string.email_is_required), state.email.error)
        assertEquals(UiText.StringResource(R.string.password_is_required), state.password.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `login with valid fields calls client with the typed credentials`() = runTest {
        coEvery { authClient.login(any()) } returns Resource.Success(tokenPair)
        typeCredentials()

        viewModel.onAction(LoginAction.LoginClicked)

        coVerify(exactly = 1) { authClient.login(LoginRequest(VALID_EMAIL, VALID_PASSWORD)) }
    }

    @Test
    fun `login shows loading while the request is in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { authClient.login(any()) } coAnswers {
            gate.await()
            Resource.Success(tokenPair)
        }
        typeCredentials()

        viewModel.onAction(LoginAction.LoginClicked)
        assertTrue(state.isLoading)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(state.isLoading)
    }

    @Test
    fun `login clicked while loading is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { authClient.login(any()) } coAnswers {
            gate.await()
            Resource.Success(tokenPair)
        }
        typeCredentials()

        viewModel.onAction(LoginAction.LoginClicked)
        viewModel.onAction(LoginAction.LoginClicked)
        gate.complete(Unit)
        advanceUntilIdle()

        coVerify(exactly = 1) { authClient.login(any()) }
    }

    @Test
    fun `login success stops loading and navigates to root clearing auth graph`() = runTest {
        coEvery { authClient.login(any()) } returns Resource.Success(tokenPair)
        typeCredentials()

        viewModel.onAction(LoginAction.LoginClicked)

        assertFalse(state.isLoading)
        coVerify(exactly = 1) { navigator.navigate(Destination.Graph.Root, homeNavOptions) }
    }

    @Test
    fun `login failure shows formatted error and stops loading`() = runTest {
        coEvery { authClient.login(any()) } returns
            Resource.Error(makaluError(401, "Invalid credentials", "MK-12"))
        typeCredentials()

        viewModel.onAction(LoginAction.LoginClicked)

        assertEquals(UiText.PlainText("MK-12 - Invalid credentials"), state.generalError)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    private companion object {
        const val VALID_EMAIL = "user@makalu.com"
        const val VALID_PASSWORD = "secret123"
    }
}
