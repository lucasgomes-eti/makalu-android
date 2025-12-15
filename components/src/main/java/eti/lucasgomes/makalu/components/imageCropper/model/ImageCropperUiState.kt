package eti.lucasgomes.makalu.components.imageCropper.model

import android.graphics.Bitmap

data class ImageCropperUiState(
    val bitmap: Bitmap? = null,
    val isImagePickerVisible: Boolean = false,
    val isPermissionDeniedDialogVisible: Boolean = false
)