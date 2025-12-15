package eti.lucasgomes.makalu.components.imageCropper.model

import android.graphics.Bitmap
import android.net.Uri

sealed interface ImageCropperAction {
    data class ImageCropped(val imageBitmap: Bitmap) : ImageCropperAction
    data object ScreenCreated : ImageCropperAction
    data object ScreenDestroyed : ImageCropperAction
    data object AddImageClicked : ImageCropperAction
    data class GalleryImageObtained(val uri: Uri?) : ImageCropperAction
    data class CameraImageTaken(val isImageSaved: Boolean) : ImageCropperAction
    data class PermissionLauncherResultReceived(
        val isGranted: Boolean,
        val launchCamera: (Uri) -> Unit
    ) : ImageCropperAction

    data object GoToSystemSettingsClicked : ImageCropperAction
    data object PermissionDeniedDialogDismissed : ImageCropperAction
    data object ImagePickerDismissed : ImageCropperAction
}