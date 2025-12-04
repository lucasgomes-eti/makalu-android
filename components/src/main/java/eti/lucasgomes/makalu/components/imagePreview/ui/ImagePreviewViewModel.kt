package eti.lucasgomes.makalu.components.imagePreview.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.components.imagePreview.model.ImagePreviewAction
import eti.lucasgomes.makalu.shared.navigation.Navigator

class ImagePreviewViewModel(private val navigator: Navigator) : ViewModel() {

    fun onAction(action: ImagePreviewAction) {
        when (action) {
            is ImagePreviewAction.ImageCropped -> onImageCropped(action.imageBitmap)
        }
    }

    private fun onImageCropped(imageBitmap: ImageBitmap) = withViewModelScope {
        navigator.navigateUpWithResult("imagePreviewResult", "my image cropped")
    }
}