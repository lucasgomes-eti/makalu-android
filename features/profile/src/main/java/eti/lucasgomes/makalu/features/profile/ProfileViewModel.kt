package eti.lucasgomes.makalu.features.profile

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.shared.network.LogoutUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.LogoutClicked -> onLogoutClicked()
            ProfileAction.ErrorDismiss -> onErrorDismiss()
            ProfileAction.InitialFetch -> onInitialFetch()
        }
    }

    private fun onLogoutClicked() = withViewModelScope { logoutUseCase() }

    private fun onErrorDismiss() = withViewModelScope {
        _uiState.update { state -> state.copy(generalError = UiText.Empty) }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        delay(5000)
        _uiState.update { state -> state.copy(isLoading = false) }
    }
}