package eti.lucasgomes.makalu.features.auth.registration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationUiState
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel(private val navigator: Navigator) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: RegistrationAction) {
        when (action) {
            is RegistrationAction.EmailChanged -> onEmailChanged(action.text)
            RegistrationAction.ProfileImageClicked -> onProfileImageClicked()
        }
    }

    private fun onEmailChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(email = text) }
    }

    private fun onProfileImageClicked() = withViewModelScope {
        navigator.navigate(Destination.Screen.ImagePreview)
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