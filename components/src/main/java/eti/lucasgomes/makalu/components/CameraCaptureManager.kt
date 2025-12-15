package eti.lucasgomes.makalu.components

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import eti.lucasgomes.makalu.shared.FILE_PROVIDER_AUTHORITY
import eti.lucasgomes.makalu.shared.IMAGE_TEMP_FILE_SUFFIX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CameraCaptureManager(
    private val cacheDir: File,
    private val app: Application
) {
    var imageUri: Uri? = null
        private set

    suspend fun capture(filePrefix: String, launchCamera: (Uri) -> Unit) {
        withContext(Dispatchers.IO) {
            imageUri = createCameraImageUri(createTempFile(filePrefix))
            withContext(Dispatchers.Main) {
                launchCamera(imageUri!!)
            }
        }
    }

    private fun createTempFile(filePrefix: String) = File.createTempFile(
        filePrefix,
        IMAGE_TEMP_FILE_SUFFIX,
        cacheDir
    )

    private fun createCameraImageUri(tempFile: File) = FileProvider.getUriForFile(
        app,
        FILE_PROVIDER_AUTHORITY, /* needs to match the provider information in the manifest */
        tempFile
    )
}