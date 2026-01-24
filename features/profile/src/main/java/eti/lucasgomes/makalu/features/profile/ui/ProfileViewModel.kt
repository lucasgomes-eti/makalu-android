package eti.lucasgomes.makalu.features.profile.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.profile.ProfileClient
import eti.lucasgomes.makalu.features.profile.model.ProfileAction
import eti.lucasgomes.makalu.features.profile.model.ProfileUiState
import eti.lucasgomes.makalu.shared.network.LogoutUseCase
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val profileClient: ProfileClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.LogoutClicked -> onLogoutClicked()
            ProfileAction.ErrorDismiss -> onErrorDismiss()
            ProfileAction.InitialFetch -> onInitialFetch()
            ProfileAction.LogoutDialogDismissed -> onLogoutDialogDismissed()
            ProfileAction.LogoutConfirmed -> onLogoutConfirmed()
        }
    }

    private fun onLogoutClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isLogoutDialogVisible = true) }
    }

    private fun onErrorDismiss() = withViewModelScope {
        _uiState.update { state -> state.copy(generalError = UiText.Empty) }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        profileClient.getSelfProfile().onError {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    generalError = UiText.PlainText(it.formatedMessage)
                )
            }
        }.onSuccess { response ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    name = response.name,
                    email = response.email,
                    phoneNumber = response.phoneNumber
                )
            }
        }
    }

    private fun onLogoutDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isLogoutDialogVisible = false) }
    }

    private fun onLogoutConfirmed() = withViewModelScope {
        _uiState.update { state -> state.copy(isLogoutDialogVisible = false) }
        logoutUseCase()
    }
}