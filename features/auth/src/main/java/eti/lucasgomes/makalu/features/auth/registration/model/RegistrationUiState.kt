package eti.lucasgomes.makalu.features.auth.registration.model

data class RegistrationUiState(
    val email: String = "",
    val profileImage: Any? = null,
    val isImagePickerVisible: Boolean = false
) {
    val isProfileImageLoaded get() = profileImage != null
}
