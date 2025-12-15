package eti.lucasgomes.makalu.features.auth.registration.ui

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.CameraCaptureManager
import eti.lucasgomes.makalu.components.ext.openApplicationSettings
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationUiState
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistrationViewModel(
    private val navigator: Navigator,
    private val app: Application,
    private val cameraCaptureManager: CameraCaptureManager
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: RegistrationAction) {
        when (action) {
            RegistrationAction.ProfileImageClicked -> onProfileImageClicked()
            RegistrationAction.ImagePickerDismissed -> onImagePickerDismissed()
            is RegistrationAction.GalleryImageObtained -> onGalleryImageObtained(action.uri)
            is RegistrationAction.CameraImageTaken -> onCameraImageTaken(action.isImageSaved)
            is RegistrationAction.PermissionLauncherResultReceived -> onPermissionLauncherResultReceived(
                action.isGranted,
                action.launchCamera
            )

            RegistrationAction.GoToSystemSettingsClicked -> onGoToSystemSettingsClicked()
            RegistrationAction.PermissionDeniedDialogDismissed -> onPermissionDeniedDialogDismissed()
            is RegistrationAction.EmailChanged -> onEmailChanged(action.text)
            is RegistrationAction.NameChanged -> onNameChanged(action.text)
            is RegistrationAction.PhoneNumberChanged -> onPhoneNumberChanged(action.text)
            is RegistrationAction.PasswordChanged -> onPasswordChanged(action.text)
            RegistrationAction.ShowPasswordClicked -> onShowPasswordClicked()
            is RegistrationAction.PasswordConfirmationChanged -> onPasswordConfirmationChanged(
                action.text
            )

            RegistrationAction.CreateAccountClicked -> onCreateAccountClicked()
        }
    }

    private fun onProfileImageClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = true) }
    }

    private fun onImagePickerDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy() }
    }

    private fun onGalleryImageObtained(uri: Uri?) = withViewModelScope {
        val outUri = navigator.navigateForResult<String>(
            Destination.Screen.ImagePreview(uri.toString()),
            RequestKey.ImageCroppedUriOutput
        ).toUri()

        _uiState.update { state ->
            state.copy(profileImage = outUri)
        }
    }

    private fun onCameraImageTaken(isImageSaved: Boolean) = withViewModelScope {
        if (isImageSaved) {
            val outUri = navigator.navigateForResult<String>(
                Destination.Screen.ImagePreview(cameraCaptureManager.imageUri.toString()),
                RequestKey.ImageCroppedUriOutput
            ).toUri()

            _uiState.update { state ->
                state.copy(profileImage = outUri)
            }
        }
    }

    private fun onPermissionLauncherResultReceived(
        isGranted: Boolean,
        launchCamera: (Uri) -> Unit
    ) = withViewModelScope {
        if (isGranted) {
            cameraCaptureManager.capture(IMAGE_TEMP_FILE_PREFIX) {
                launchCamera(it)
            }
        } else {
            _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = true) }
        }
    }

    private fun onGoToSystemSettingsClicked() = withViewModelScope {
        _uiState.update { state -> state.copy() }
        app.openApplicationSettings()
    }

    private fun onPermissionDeniedDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy() }
    }

    private fun onNameChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(name = text) }
    }

    private fun onEmailChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(email = text) }
    }

    private fun onPhoneNumberChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(phoneNumber = text) }
    }

    private fun onPasswordChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(password = text) }
    }

    private fun onShowPasswordClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isPasswordVisible = state.isPasswordVisible.not()) }
    }

    private fun onPasswordConfirmationChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(passwordConfirmation = text) }
    }

    private fun onCreateAccountClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        delay(2_000L)
        navigator.navigate(
            Destination.Graph.Home,
            NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true))
        )
    }

    companion object {
        private const val IMAGE_TEMP_FILE_PREFIX = "profile_pic_"
    }
}