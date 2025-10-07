package eti.lucasgomes.makalu.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eti.lucasgomes.makalu.components.ext.withScreenModelScope
import eti.lucasgomes.makalu.features.auth.login.model.LoginAction
import eti.lucasgomes.makalu.features.auth.login.model.LoginUiState
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val navigator: Navigator) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.LoginChanged -> onLoginChanged(action.text)
            is LoginAction.PasswordChanged -> onPasswordChanged(action.text)
            LoginAction.ShowPasswordClicked -> onShowPasswordClicked()
            LoginAction.LoginClicked -> onLoginClicked()
            LoginAction.RegistrationClicked -> onRegistrationClicked()
        }
    }

    private fun onLoginChanged(text: String) = withScreenModelScope {
        _uiState.update { state -> state.copy(email = text) }
    }

    private fun onPasswordChanged(text: String) = withScreenModelScope {
        _uiState.update { state -> state.copy(password = text) }
    }

    private fun onShowPasswordClicked() = withScreenModelScope {
        _uiState.update { state -> state.copy(isPasswordVisible = state.isPasswordVisible.not()) }
    }

    private fun onLoginClicked() = withScreenModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        delay(2000)
        _uiState.update { state -> state.copy(isLoading = false) }
    }

    private fun onRegistrationClicked() = withScreenModelScope {
        navigator.navigate(Destination.Screen.Registration)
    }

    private fun goToHome() {
        viewModelScope.launch {
            navigator.navigate(
                Destination.Graph.Home,
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true))
            )
        }
    }
}