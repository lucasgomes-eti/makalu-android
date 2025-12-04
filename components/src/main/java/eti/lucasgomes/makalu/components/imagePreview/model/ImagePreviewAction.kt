package eti.lucasgomes.makalu.components.imagePreview.model

import androidx.compose.ui.graphics.ImageBitmap

sealed interface ImagePreviewAction {
    data class ImageCropped(val imageBitmap: ImageBitmap) : ImagePreviewAction
}