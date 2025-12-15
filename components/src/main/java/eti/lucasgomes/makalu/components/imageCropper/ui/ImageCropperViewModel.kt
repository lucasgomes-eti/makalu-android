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
import eti.lucasgomes.makalu.components.ext.openApplicationSettings
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperAction
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperUiState
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

    fun onAction(action: ImageCropperAction) {
        when (action) {
            is ImageCropperAction.ImageCropped -> onImageCropped(action.imageBitmap)
            ImageCropperAction.ScreenCreated -> onScreenCreated()
            ImageCropperAction.ScreenDestroyed -> onScreenDestroyed()
            ImageCropperAction.AddImageClicked -> onAddImageClicked()
            is ImageCropperAction.CameraImageTaken -> onCameraImageTaken(action.isImageSaved)
            is ImageCropperAction.GalleryImageObtained -> onGalleryImageObtained(action.uri)
            ImageCropperAction.GoToSystemSettingsClicked -> onGoToSystemSettingsClicked()
            ImageCropperAction.ImagePickerDismissed -> onImagePickerDismissed()
            ImageCropperAction.PermissionDeniedDialogDismissed -> onPermissionDeniedDialogDismissed()
            is ImageCropperAction.PermissionLauncherResultReceived -> onPermissionLauncherResultReceived(
                action.isGranted,
                action.launchCamera
            )
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
        createInitialBitmap(uriStr.toUri())
    }

    private suspend fun createInitialBitmap(uri: Uri) {
        withContext(Dispatchers.IO) {
            val bitmap = ImageDecoder
                .createSource(contentResolver, uri)
                .decodeBitmap { _, _ -> }
            _uiState.update { state -> state.copy(bitmap = bitmap) }
        }
    }

    private fun onScreenDestroyed() = withViewModelScope {
        _uiState.update { state -> state.copy(bitmap = null) }
    }

    private fun onAddImageClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = true) }
    }

    private fun onCameraImageTaken(isImageSaved: Boolean) = withViewModelScope {
        if (isImageSaved) {
            cameraCaptureManager.imageUri?.let { createInitialBitmap(it) }
            _uiState.update { state ->
                state.copy(isImagePickerVisible = false)
            }
        }
    }

    private fun onGalleryImageObtained(uri: Uri?) = withViewModelScope {
        uri?.let { createInitialBitmap(it) }
        _uiState.update { state ->
            state.copy(isImagePickerVisible = false)
        }
    }

    private fun onGoToSystemSettingsClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isPermissionDeniedDialogVisible = false) }
        app.openApplicationSettings()
    }

    private fun onImagePickerDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isImagePickerVisible = false) }
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
    }
}