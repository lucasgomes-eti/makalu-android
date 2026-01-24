package eti.lucasgomes.makalu.features.auth.login.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.auth.AuthClient
import eti.lucasgomes.makalu.features.auth.R
import eti.lucasgomes.makalu.features.auth.login.model.LoginAction
import eti.lucasgomes.makalu.features.auth.login.model.LoginRequest
import eti.lucasgomes.makalu.features.auth.login.model.LoginUiState
import eti.lucasgomes.makalu.shared.MIN_PASSWORD_LENGTH
import eti.lucasgomes.makalu.shared.REGEX_EMAIL
import eti.lucasgomes.makalu.shared.REGEX_PASSWORD
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val authClient: AuthClient,
    private val navigator: Navigator
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> onLoginChanged(action.text)
            is LoginAction.PasswordChanged -> onPasswordChanged(action.text)
            LoginAction.ShowPasswordClicked -> onShowPasswordClicked()
            LoginAction.LoginClicked -> onLoginClicked()
            LoginAction.RegistrationClicked -> onRegistrationClicked()
            LoginAction.DismissError -> onDismissError()
        }
    }

    private fun onLoginChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(email = state.email.copy(text = text)) }
    }

    private fun onPasswordChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(password = state.password.copy(text = text)) }
    }

    private fun onShowPasswordClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isPasswordVisible = state.isPasswordVisible.not()) }
    }

    private fun onLoginClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }

        withValidUiState { request ->
            authClient.login(request).onError {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        generalError = UiText.PlainText(it.formatedMessage)
                    )
                }
            }.onSuccess {
                _uiState.update { state -> state.copy(isLoading = false) }
                navigator.navigate(
                    Destination.Graph.Home,
                    NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true))
                )
            }
        }
    }

    private fun onRegistrationClicked() = withViewModelScope {
        navigator.navigate(Destination.Screen.Registration)
    }

    private fun onDismissError() =
        withViewModelScope { _uiState.update { state -> state.copy(generalError = UiText.Empty) } }

    private suspend fun withValidUiState(block: suspend (LoginRequest) -> Unit) {
        val state = uiState.value
        val isStateValid = isEmailValid(state.email.text) and isPasswordValid(state.password.text)
        if (isStateValid) {
            block(LoginRequest(email = state.email.text, password = state.password.text))
        } else {
            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        if (email.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    email = state.email.copy(error = UiText.StringResource(R.string.email_is_required))
                )
            }
            return false
        }
        if (email.matches(REGEX_EMAIL).not()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    email = state.email.copy(error = UiText.StringResource(R.string.email_must_have_valid_format))
                )
            }
            return false
        }
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    password = state.password.copy(error = UiText.StringResource(R.string.password_is_required))
                )
            }
            return false
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    password = state.password.copy(
                        error = UiText.StringResource(
                            R.string.password_must_have_at_least_characters,
                            listOf(MIN_PASSWORD_LENGTH)
                        )
                    )
                )
            }
            return false
        }
        if (password.matches(REGEX_PASSWORD).not()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    password = state.password.copy(error = UiText.StringResource(R.string.password_must_have_letters_and_numbers))
                )
            }
            return false
        }

        return true
    }
}