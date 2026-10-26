package eti.lucasgomes.makalu.components.imageCropper.model

import android.graphics.Bitmap
import eti.lucasgomes.makalu.components.dsl.UiText

data class ImageCropperUiState(
    val bitmap: Bitmap? = null,
    val isImagePickerVisible: Boolean = false,
    val isPermissionDeniedDialogVisible: Boolean = false,
    val error: UiText = UiText.Empty
)
