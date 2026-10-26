package eti.lucasgomes.makalu.components.imageCropper.ui

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.decodeBitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.CameraCaptureManager
import eti.lucasgomes.makalu.components.R
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.openApplicationSettings
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperAction
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperUiState
import eti.lucasgomes.makalu.shared.FILE_PROVIDER_AUTHORITY
import eti.lucasgomes.makalu.shared.IMAGE_TEMP_FILE_SUFFIX
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ImageCropperViewModel(
    private val navigator: Navigator,
    private val contentResolver: ContentResolver,
    private val cacheDir: File,
    private val app: Application,
    private val cameraCaptureManager: CameraCaptureManager,
    private val uriStr: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageCropperUiState())
    val uiState = _uiState.asStateFlow()

    init {
        withViewModelScope { loadBitmap(uriStr.toUri()) }
    }

    fun onAction(action: ImageCropperAction) {
        when (action) {
            is ImageCropperAction.ImageCropped -> onImageCropped(action.imageBitmap)

            ImageCropperAction.AddImageClicked -> onAddImageClicked()

            is ImageCropperAction.CameraImageTaken -> onCameraImageTaken(action.isImageSaved)

            is ImageCropperAction.GalleryImageObtained -> onGalleryImageObtained(action.uri)

            ImageCropperAction.GoToSystemSettingsClicked -> onGoToSystemSettingsClicked()

            ImageCropperAction.ImagePickerDismissed -> onImagePickerDismissed()

            ImageCropperAction.ErrorDismissed -> onErrorDismissed()

            ImageCropperAction.PermissionDeniedDialogDismissed ->
                onPermissionDeniedDialogDismissed()

            is ImageCropperAction.PermissionLauncherResultReceived ->
                onPermissionLauncherResultReceived(action.isGranted, action.launchCamera)
        }
    }

    private fun onImageCropped(imageBitmap: Bitmap) = withViewModelScope {
        runCatching {
            withContext(Dispatchers.IO) { createImageOutputUri(writeBitmapToFile(imageBitmap)) }
        }.onSuccess { uri ->
            navigator.navigateUpWithResult(RequestKey.ImageCroppedUriOutput, uri.toString())
        }.onFailure {
            _uiState.update { state ->
                state.copy(error = UiText.StringResource(R.string.image_save_error))
            }
        }
    }

    private fun writeBitmapToFile(bitmap: Bitmap): File {
        val tempFile = File.createTempFile(
            IMAGE_TEMP_FILE_PREFIX,
            IMAGE_TEMP_FILE_SUFFIX,
            cacheDir
        )
        tempFile.writeBytes(compressToJpeg(bitmap))
        return tempFile
    }

    private fun compressToJpeg(bitmap: Bitmap): ByteArray {
        var quality = MAX_JPEG_QUALITY
        while (true) {
            val bytes = ByteArrayOutputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
                it.toByteArray()
            }
            if (bytes.size <= MAX_OUTPUT_BYTES || quality <= MIN_JPEG_QUALITY) return bytes
            quality -= JPEG_QUALITY_STEP
        }
    }

    private fun createImageOutputUri(tempFile: File) = FileProvider.getUriForFile(
        app,
        FILE_PROVIDER_AUTHORITY, /* needs to match the provider information in the manifest */
        tempFile
    )

    private suspend fun loadBitmap(uri: Uri) {
        runCatching { withContext(Dispatchers.IO) { decodeBitmap(uri) } }
            .onSuccess { bitmap ->
                _uiState.update { state -> state.copy(bitmap = bitmap, error = UiText.Empty) }
            }
            .onFailure {
                _uiState.update { state ->
                    state.copy(error = UiText.StringResource(R.string.image_load_error))
                }
            }
    }

    private fun decodeBitmap(uri: Uri): Bitmap =
        ImageDecoder.createSource(contentResolver, uri).decodeBitmap { info, _ ->
            val longEdge = maxOf(info.size.width, info.size.height)
            if (longEdge > MAX_IMAGE_EDGE_PX) {
                val scale = MAX_IMAGE_EDGE_PX.toFloat() / longEdge
                setTargetSize(
                    (info.size.width * scale).roundToInt().coerceAtLeast(1),
                    (info.size.height * scale).roundToInt().coerceAtLeast(1)
                )
            }
        }

    private fun onAddImageClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = true) }
    }

    private fun onCameraImageTaken(isImageSaved: Boolean) = withViewModelScope {
        if (isImageSaved) {
            _uiState.update { state -> state.copy(isImagePickerVisible = false) }
            cameraCaptureManager.imageUri?.let { loadBitmap(it) }
        }
    }

    private fun onGalleryImageObtained(uri: Uri?) = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = false) }
        uri?.let { loadBitmap(it) }
    }

    private fun onGoToSystemSettingsClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = false) }
        app.openApplicationSettings()
    }

    private fun onImagePickerDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = false) }
    }

    private fun onErrorDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(error = UiText.Empty) }
    }

    private fun onPermissionDeniedDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = false) }
    }

    private fun onPermissionLauncherResultReceived(
        isGranted: Boolean,
        launchCamera: (Uri) -> Unit
    ) = withViewModelScope {
        if (isGranted) {
            cameraCaptureManager.capture(IMAGE_TEMP_FILE_PREFIX) { uri ->
                launchCamera(uri)
            }
        } else {
            _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = true) }
        }
    }

    companion object {
        private const val IMAGE_TEMP_FILE_PREFIX = "cropped_profile_pic_"
        private const val MAX_IMAGE_EDGE_PX = 2048
        private const val MAX_OUTPUT_BYTES = 2 * 1024 * 1024
        private const val MAX_JPEG_QUALITY = 100
        private const val MIN_JPEG_QUALITY = 60
        private const val JPEG_QUALITY_STEP = 10
    }
}
