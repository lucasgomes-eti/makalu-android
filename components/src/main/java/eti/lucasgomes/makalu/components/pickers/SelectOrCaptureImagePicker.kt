package eti.lucasgomes.makalu.components.pickers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import eti.lucasgomes.makalu.components.R

@Composable
fun SelectOrCaptureImagePicker(
    onGalleryImageObtained: (uri: Uri?) -> Unit,
    onCameraImageTaken: (isImageSaved: Boolean) -> Unit,
    onPermissionLauncherResultReceived: (permissionGranted: Boolean, launchCamera: (Uri) -> Unit) -> Unit,
    onDismissRequest: () -> Unit
) {
    val pickImageFromGalleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            onGalleryImageObtained(uri)
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isImageSaved ->
            onCameraImageTaken(isImageSaved)
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        onPermissionLauncherResultReceived(permissionGranted) {
            cameraLauncher.launch(it)
        }
    }

    SelectOrCaptureImagePickerComponent(
        onSelectGalleryImage = {
            val mediaRequest =
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            pickImageFromGalleryLauncher.launch(mediaRequest)
        },
        onCaptureCameraImage = {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        },
        onDismissRequest = onDismissRequest
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectOrCaptureImagePickerComponent(
    onSelectGalleryImage: () -> Unit = {},
    onCaptureCameraImage: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column {
            ListItem(
                headlineContent = { Text(stringResource(R.string.select_image_from_gallery)) },
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.image),
                        contentDescription = null,
                        tint = colorScheme.onSurface
                    )
                },
                modifier = Modifier.Companion.clickable(
                    role = Role.Companion.Button,
                    onClick = onSelectGalleryImage
                )
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.capture_image_with_camera)) },
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.camera),
                        contentDescription = null,
                        tint = colorScheme.onSurface
                    )
                },
                modifier = Modifier.Companion.clickable(
                    role = Role.Companion.Button,
                    onClick = onCaptureCameraImage
                )
            )
        }
    }
}
