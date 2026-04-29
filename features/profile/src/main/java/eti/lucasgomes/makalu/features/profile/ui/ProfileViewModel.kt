package eti.lucasgomes.makalu.features.profile.ui

import MakaluConfig
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.CameraCaptureManager
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.profile.ProfileClient
import eti.lucasgomes.makalu.features.profile.model.ProfileAction
import eti.lucasgomes.makalu.features.profile.model.ProfileUiState
import eti.lucasgomes.makalu.shared.PROFILE_PIC_TEMP_FILE_PREFIX
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.OSNavigation
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import eti.lucasgomes.makalu.shared.network.LogoutUseCase
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val profileClient: ProfileClient,
    private val navigator: Navigator,
    private val cameraCaptureManager: CameraCaptureManager,
    private val osNavigation: OSNavigation,
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
            ProfileAction.ProfileImageClicked -> onProfileImageClicked()
            is ProfileAction.CameraImageTaken -> onCameraImageTaken(action.isImageSaved)
            is ProfileAction.GalleryImageObtained -> onGalleryImageObtained(action.uri)
            ProfileAction.GoToSystemSettingsClicked -> onGoToSystemSettingsClicked()
            ProfileAction.ImagePickerDismissed -> onImagePickerDismissed()
            ProfileAction.PermissionDeniedDialogDismissed -> onPermissionDeniedDialogDismissed()
            is ProfileAction.PermissionLauncherResultReceived -> onPermissionLauncherResultReceived(
                action.isGranted,
                action.launchCamera
            )
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
                    phoneNumber = response.phoneNumber,
                    imageUrlString = mapImageIdToUrl(response.profileImageId)
                )
            }
        }
    }

    private fun mapImageIdToUrl(imageId: Long?): String? = imageId?.let { id ->
        "${MakaluConfig.BASE_URL}profile/image/$id"
    }

    private fun onLogoutDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isLogoutDialogVisible = false) }
    }

    private fun onLogoutConfirmed() = withViewModelScope {
        _uiState.update { state -> state.copy(isLogoutDialogVisible = false) }
        logoutUseCase()
    }

    private fun onProfileImageClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = true) }
    }

    private fun onCameraImageTaken(isImageSaved: Boolean) = withViewModelScope {
        if (isImageSaved) {
            val outUri = navigator.navigateForResult<String>(
                Destination.Screen.ImagePreview(cameraCaptureManager.imageUri.toString()),
                RequestKey.ImageCroppedUriOutput
            ).toUri()

            _uiState.update { state ->
                state.copy(
                    imageUploadLoading = true,
                    isImagePickerVisible = false
                )
            }

            uploadImage(outUri)
        }
    }

    private fun onGalleryImageObtained(uri: Uri?) = withViewModelScope {
        if (uri != null) {
            val outUri = navigator.navigateForResult<String>(
                Destination.Screen.ImagePreview(uri.toString()),
                RequestKey.ImageCroppedUriOutput
            ).toUri()

            _uiState.update { state ->
                state.copy(
                    imageUploadLoading = true,
                    isImagePickerVisible = false
                )
            }

            uploadImage(outUri)
        }
    }

    private suspend fun uploadImage(outUri: Uri) {
        profileClient.uploadImage(outUri).onError {
            _uiState.update { state ->
                state.copy(
                    imageUploadLoading = false,
                    generalError = UiText.PlainText(it.formatedMessage)
                )
            }
        }.onSuccess {
            _uiState.update { state ->
                state.copy(
                    imageUploadLoading = false,
                    imageUrlString = mapImageIdToUrl(it.id)
                )
            }
        }
    }

    private fun onGoToSystemSettingsClicked() = withViewModelScope {
        osNavigation.openApplicationSettings()
    }

    private fun onImagePickerDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = false) }
    }

    private fun onPermissionDeniedDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy() }
    }

    private fun onPermissionLauncherResultReceived(
        isGranted: Boolean,
        launchCamera: (Uri) -> Unit
    ) = withViewModelScope {
        if (isGranted) {
            cameraCaptureManager.capture(PROFILE_PIC_TEMP_FILE_PREFIX) {
                launchCamera(it)
            }
        } else {
            _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = true) }
        }
    }
}
