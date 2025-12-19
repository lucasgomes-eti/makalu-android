package eti.lucasgomes.makalu.features.auth.registration.model

import eti.lucasgomes.makalu.components.dsl.UiText

data class RegistrationUiState(
    val profileImage: Any? = null,
    val isImagePickerVisible: Boolean = false,
    val isPermissionDeniedDialogVisible: Boolean = false,
    val name: String = "",
    val nameError: UiText = UiText.Empty,
    val email: String = "",
    val emailError: UiText = UiText.Empty,
    val phoneNumber: String = "",
    val phoneNumberError: UiText = UiText.Empty,
    val password: String = "",
    val passwordError: UiText = UiText.Empty,
    val isPasswordVisible: Boolean = false,
    val passwordConfirmation: String = "",
    val passwordConfirmationError: UiText = UiText.Empty,
    val isLoading: Boolean = false
) {
    val isProfileImageLoaded get() = profileImage != null
}
