package eti.lucasgomes.makalu.features.auth.registration.model

import android.net.Uri

sealed interface RegistrationAction {
    data class EmailChanged(val text: String) : RegistrationAction
    data object ProfileImageClicked : RegistrationAction
    data object ImagePickerDismissed : RegistrationAction
    data class GalleryImageObtained(val uri: Uri?) : RegistrationAction
    data class CameraImageTaken(val isImageSaved: Boolean) : RegistrationAction
    data class PermissionLauncherResultReceived(
        val isGranted: Boolean,
        val launchCamera: (Uri) -> Unit
    ) : RegistrationAction
}