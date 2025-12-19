package eti.lucasgomes.makalu.features.auth.registration.ui

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.CameraCaptureManager
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.openApplicationSettings
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.auth.R
import eti.lucasgomes.makalu.features.auth.registration.model.CreateAccountRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationUiState
import eti.lucasgomes.makalu.shared.REGEX_EMAIL
import eti.lucasgomes.makalu.shared.REGEX_PASSWORD
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
) : ViewModel() {

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
        if (text.length > MAX_NAME_LENGTH)
            return@withViewModelScope

        _uiState.update { state -> state.copy(name = text, nameError = UiText.Empty) }
    }

    private fun onEmailChanged(text: String) = withViewModelScope {
        if (text.length > MAX_EMAIL_LENGTH)
            return@withViewModelScope

        _uiState.update { state -> state.copy(email = text, emailError = UiText.Empty) }
    }

    private fun onPhoneNumberChanged(text: String) = withViewModelScope {
        if (text.length > MAX_PHONE_NUMBER_LENGTH)
            return@withViewModelScope

        _uiState.update { state -> state.copy(phoneNumber = text, phoneNumberError = UiText.Empty) }
    }

    private fun onPasswordChanged(text: String) = withViewModelScope {
        if (text.length > MAX_PASSWORD_LENGTH)
            return@withViewModelScope

        _uiState.update { state ->
            state.copy(
                password = text,
                passwordError = UiText.Empty,
                passwordConfirmationError = UiText.Empty
            )
        }
    }

    private fun onShowPasswordClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isPasswordVisible = state.isPasswordVisible.not()) }
    }

    private fun onPasswordConfirmationChanged(text: String) = withViewModelScope {
        if (text.length > MAX_PASSWORD_LENGTH)
            return@withViewModelScope

        _uiState.update { state ->
            state.copy(
                passwordConfirmation = text,
                passwordConfirmationError = UiText.Empty,
                passwordError = UiText.Empty
            )
        }
    }

    private fun onCreateAccountClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        withValidUiState {
            delay(2_000L)
            navigator.navigate(
                Destination.Graph.Home,
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true))
            )
        }
    }

    private suspend fun withValidUiState(block: suspend (CreateAccountRequest) -> Unit) {
        val state = uiState.value
        val isStateValid =
            isNameValid(state.name) and
                    isEmailValid(state.email) and
                    isPhoneNumberValid(state.phoneNumber) and
                    isPasswordValid(
                        state.password,
                        state.passwordConfirmation
                    )

        if (isStateValid)
            block(buildCreateAccountRequest(state))
    }

    private fun isNameValid(name: String): Boolean {
        if (name.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    nameError = UiText.StringResource(R.string.name_is_required)
                )
            }
            return false
        }
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        if (email.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    emailError = UiText.StringResource(R.string.email_is_required)
                )
            }
            return false
        }
        if (email.matches(REGEX_EMAIL).not()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    emailError = UiText.StringResource(R.string.email_must_have_valid_format)
                )
            }
            return false
        }
        return true
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    phoneNumberError = UiText.StringResource(R.string.phone_number_is_required)
                )
            }
            return false
        }

        return true
    }

    private fun isPasswordValid(password: String, passwordConfirmation: String): Boolean {
        if (password.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    passwordError = UiText.StringResource(R.string.password_is_required)
                )
            }
            return false
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    passwordError = UiText.StringResource(
                        R.string.password_must_have_at_least_characters,
                        listOf(MIN_PASSWORD_LENGTH)
                    )
                )
            }
            return false
        }
        if (password.matches(REGEX_PASSWORD).not()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    passwordError = UiText.StringResource(R.string.password_must_have_letters_and_numbers)
                )
            }
            return false
        }

        if (password != passwordConfirmation) {
            val error = UiText.StringResource(R.string.passwords_do_not_match)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    passwordError = error,
                    passwordConfirmationError = error
                )
            }
            return false
        }

        return true
    }

    private fun buildCreateAccountRequest(state: RegistrationUiState): CreateAccountRequest {
        return CreateAccountRequest(
            name = state.name,
            email = state.email,
            phoneNumber = state.phoneNumber,
            password = state.password
        )
    }

    companion object {
        private const val IMAGE_TEMP_FILE_PREFIX = "profile_pic_"
        private const val MAX_NAME_LENGTH = 120
        private const val MAX_EMAIL_LENGTH = 120
        private const val MAX_PHONE_NUMBER_LENGTH = 15
        private const val MAX_PASSWORD_LENGTH = 25
        private const val MIN_PASSWORD_LENGTH = 8
    }
}