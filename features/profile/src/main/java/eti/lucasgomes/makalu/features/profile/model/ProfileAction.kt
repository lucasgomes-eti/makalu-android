package eti.lucasgomes.makalu.features.profile.model

import android.net.Uri

sealed interface ProfileAction {
    data object LogoutClicked : ProfileAction
    data object ErrorDismiss : ProfileAction
    data object InitialFetch : ProfileAction
    data object LogoutDialogDismissed : ProfileAction
    data object LogoutConfirmed : ProfileAction
    data object ProfileImageClicked : ProfileAction
    data class GalleryImageObtained(val uri: Uri?) : ProfileAction
    data class CameraImageTaken(val isImageSaved: Boolean) : ProfileAction
    data class PermissionLauncherResultReceived(
        val isGranted: Boolean,
        val launchCamera: (Uri) -> Unit
    ) : ProfileAction

    data object ImagePickerDismissed : ProfileAction
    data object PermissionDeniedDialogDismissed : ProfileAction
    data object GoToSystemSettingsClicked : ProfileAction
}
