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

    init {
//        withViewModelScope {
//            delay(3_000)
//            _uiState.update { state -> state.copy(profilePicture = "https://images.unsplash.com/photo-1764712754791-8627ebded3f6?q=80&w=690&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D") }
//        }
    }

    fun onAction(action: RegistrationAction) {
        when (action) {
            is RegistrationAction.EmailChanged -> onEmailChanged(action.text)
            RegistrationAction.ProfileImageClicked -> onProfileImageClicked()
            RegistrationAction.ImagePickerDismissed -> onImagePickerDismissed()
            RegistrationAction.SelectGalleryImage -> TODO()
            RegistrationAction.CaptureCameraImage -> TODO()
        }
    }

    private fun onEmailChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(email = text) }
    }

    private fun onProfileImageClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = true) }
//        val imageResult = navigator.navigateForResult<String>(
//            Destination.Screen.ImagePreview,
//            "imagePreviewResult"
//        )
//        _uiState.update { state -> state.copy(email = imageResult) }
    }

    private fun onImagePickerDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = false) }
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