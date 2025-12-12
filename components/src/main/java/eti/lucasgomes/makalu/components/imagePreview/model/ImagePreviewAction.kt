package eti.lucasgomes.makalu.components.imagePreview.model

import android.graphics.Bitmap

sealed interface ImagePreviewAction {
    data class ImageCropped(val imageBitmap: Bitmap) : ImagePreviewAction
    data object ScreenCreated : ImagePreviewAction
    data object ScreenDestroyed : ImagePreviewAction
}