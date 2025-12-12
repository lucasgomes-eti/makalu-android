package eti.lucasgomes.makalu.features.auth.registration.ui

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationUiState
import eti.lucasgomes.makalu.shared.FILE_PROVIDER_AUTHORITY
import eti.lucasgomes.makalu.shared.IMAGE_TEMP_FILE_SUFFIX
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RegistrationViewModel(
    private val navigator: Navigator,
    private val cacheDir: File,
    private val app: Application
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    private var cameraImageUri: Uri? = null

    fun onAction(action: RegistrationAction) {
        when (action) {
            is RegistrationAction.EmailChanged -> onEmailChanged(action.text)
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
        }
    }

    private fun onEmailChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(email = text) }
    }

    private fun onProfileImageClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = true) }
    }

    private fun onImagePickerDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = false) }
    }

    private fun onGalleryImageObtained(uri: Uri?) = withViewModelScope {
        val outUri = navigator.navigateForResult<String>(
            Destination.Screen.ImagePreview(uri.toString()),
            RequestKey.ImageCroppedUriOutput
        ).toUri()

        _uiState.update { state ->
            state.copy(isImagePickerVisible = false, profileImage = outUri)
        }
    }

    private fun onCameraImageTaken(isImageSaved: Boolean) = withViewModelScope {
        if (isImageSaved) {
            val outUri = navigator.navigateForResult<String>(
                Destination.Screen.ImagePreview(cameraImageUri.toString()),
                RequestKey.ImageCroppedUriOutput
            ).toUri()
            
            _uiState.update { state ->
                state.copy(isImagePickerVisible = false, profileImage = outUri)
            }
        }
    }

    private fun onPermissionLauncherResultReceived(
        isGranted: Boolean,
        launchCamera: (Uri) -> Unit
    ) = withViewModelScope {
        if (isGranted) {
            withContext(Dispatchers.IO) {
                cameraImageUri = createCameraImageUri(createTempFile())
                withContext(Dispatchers.Main) {
                    launchCamera(cameraImageUri!!)
                }
            }
        } else {
            _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = true) }
        }
    }

    private fun createTempFile() = File.createTempFile(
        IMAGE_TEMP_FILE_PREFIX,
        IMAGE_TEMP_FILE_SUFFIX,
        cacheDir
    )

    private fun createCameraImageUri(tempFile: File) = FileProvider.getUriForFile(
        app,
        FILE_PROVIDER_AUTHORITY, /* needs to match the provider information in the manifest */
        tempFile
    )

    private fun onGoToSystemSettingsClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = false) }
        app.startActivity(
            Intent(
                ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts(APP_SETTINGS_URI_SCHEME, app.packageName, null)
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun onPermissionDeniedDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = false) }
    }

    private fun goToHome() {
        viewModelScope.launch {
            navigator.navigate(
                Destination.Graph.Home,
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true))
            )
        }
    }

    companion object {
        private const val IMAGE_TEMP_FILE_PREFIX = "profile_pic_"
        private const val APP_SETTINGS_URI_SCHEME = "package"
    }
}