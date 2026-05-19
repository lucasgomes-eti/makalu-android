package eti.lucasgomes.makalu.features.profile.ui

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.mapImageUrl

data class ProfileUiState(
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isLogoutDialogVisible: Boolean = false,
    private val imageId: Long? = null,
    val isImagePickerVisible: Boolean = false,
    val isPermissionDeniedDialogVisible: Boolean = false,
    val imageUploadLoading: Boolean = false,
) {
    val isProfileImageLoaded get() = imageUrl != null

    val imageUrl: String?
        get() = mapImageUrl(imageId)
}