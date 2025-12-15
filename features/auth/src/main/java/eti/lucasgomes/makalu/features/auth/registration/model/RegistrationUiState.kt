package eti.lucasgomes.makalu.features.auth.registration.model

data class RegistrationUiState(
    val profileImage: Any? = null,
    val isImagePickerVisible: Boolean = false,
    val isPermissionDeniedDialogVisible: Boolean = false,
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordConfirmation: String = "",
    val isLoading: Boolean = false
) {
    val isProfileImageLoaded get() = profileImage != null
}
