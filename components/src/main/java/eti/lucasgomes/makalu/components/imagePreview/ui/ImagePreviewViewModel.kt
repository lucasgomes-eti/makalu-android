package eti.lucasgomes.makalu.components.imagePreview.ui

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.content.FileProvider
import androidx.core.graphics.decodeBitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.components.imagePreview.model.ImagePreviewAction
import eti.lucasgomes.makalu.components.imagePreview.model.ImagePreviewUiState
import eti.lucasgomes.makalu.shared.FILE_PROVIDER_AUTHORITY
import eti.lucasgomes.makalu.shared.IMAGE_TEMP_FILE_SUFFIX
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

class ImagePreviewViewModel(
    private val navigator: Navigator,
    private val contentResolver: ContentResolver,
    private val cacheDir: File,
    private val app: Application,
    private val uri: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImagePreviewUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: ImagePreviewAction) {
        when (action) {
            is ImagePreviewAction.ImageCropped -> onImageCropped(action.imageBitmap)
            ImagePreviewAction.ScreenCreated -> onScreenCreated()
            ImagePreviewAction.ScreenDestroyed -> onScreenDestroyed()
        }
    }

    private fun onImageCropped(imageBitmap: Bitmap) = withViewModelScope {
        withContext(Dispatchers.IO) {
            val file = writeBitmapToFile(imageBitmap)
            val uri = createImageOutputUri(file)
            withContext(Dispatchers.Main) {
                navigator.navigateUpWithResult(RequestKey.ImageCroppedUriOutput, uri.toString())
            }
        }
    }

    private fun writeBitmapToFile(bitmap: Bitmap): File {
        val tempFile = File.createTempFile(
            IMAGE_TEMP_FILE_PREFIX,
            IMAGE_TEMP_FILE_SUFFIX,
            cacheDir
        )
        tempFile.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return tempFile
    }

    private fun createImageOutputUri(tempFile: File) = FileProvider.getUriForFile(
        app,
        FILE_PROVIDER_AUTHORITY, /* needs to match the provider information in the manifest */
        tempFile
    )

    private fun onScreenCreated() = withViewModelScope {
        createInitialBitmap()
    }

    private suspend fun createInitialBitmap() {
        withContext(Dispatchers.IO) {
            val bitmap = ImageDecoder
                .createSource(contentResolver, uri.toUri())
                .decodeBitmap { _, _ -> }
            _uiState.update { state -> state.copy(bitmap = bitmap) }
        }
    }

    private fun onScreenDestroyed() = withViewModelScope {
        _uiState.update { state -> state.copy(bitmap = null) }
    }

    companion object {
        private const val IMAGE_TEMP_FILE_PREFIX = "cropped_profile_pic_"
    }
}