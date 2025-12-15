package eti.lucasgomes.makalu.components.imageCropper.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropRatio
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.GridLinesType
import com.tanishranjan.cropkit.ImageCropper
import com.tanishranjan.cropkit.rememberCropController
import eti.lucasgomes.makalu.components.CameraPermissionDeniedDialog
import eti.lucasgomes.makalu.components.R
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.appBars.TopBarAction
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperAction
import eti.lucasgomes.makalu.components.imageCropper.model.ImageCropperUiState
import eti.lucasgomes.makalu.components.pickers.SelectOrCaptureImagePicker

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ImageCropperScreen(
    uiState: ImageCropperUiState,
    onAction: (ImageCropperAction) -> Unit
) {

    DisposableEffect(Unit) {
        onAction(ImageCropperAction.ScreenCreated)
        onDispose { onAction(ImageCropperAction.ScreenDestroyed) }
    }

    ConfigureTopBar(
        title = stringResource(R.string.select_image_area), navigationActions = listOf(
            TopBarAction(
                icon = painterResource(R.drawable.add_photo),
                contentDescription = stringResource(R.string.accessibility_add_image_icon),
                onClick = { onAction(ImageCropperAction.AddImageClicked) }
            )
        )
    )

    val cropController = rememberCropController(
        bitmap = uiState.bitmap
            ?: ImageBitmap.imageResource(R.drawable.placeholder)
                .asAndroidBitmap(),
        cropOptions = CropDefaults.cropOptions(
            cropShape = CropShape.AspectRatio(CropRatio.SQUARE),
            gridLinesType = GridLinesType.GRID_AND_CIRCLE
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalFloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -ScreenOffset)
                .zIndex(1f),
            expanded = true,
            leadingContent = {
                val rotateLeftLabel = stringResource(R.string.rotate_left)
                val rotateRightLabel = stringResource(R.string.rotate_right)
                val mirrorVerticallyLabel = stringResource(R.string.mirror_vertically)
                val mirrorHorizontallyLabel = stringResource(R.string.mirror_horizontally)
                AppBarRow {
                    clickableItem(
                        onClick = {
                            cropController.rotateAntiClockwise()
                        },
                        icon = { Icon(painterResource(R.drawable.rotate_left), null) },
                        label = rotateLeftLabel
                    )
                    clickableItem(
                        onClick = {
                            cropController.rotateClockwise()
                        },
                        icon = { Icon(painterResource(R.drawable.rotate_right), null) },
                        label = rotateRightLabel
                    )
                    clickableItem(
                        onClick = {
                            cropController.flipVertically()
                        },
                        icon = { Icon(painterResource(R.drawable.swap_vert), null) },
                        label = mirrorVerticallyLabel
                    )
                    clickableItem(
                        onClick = {
                            cropController.flipHorizontally()
                        },
                        icon = { Icon(painterResource(R.drawable.swap_horiz), null) },
                        label = mirrorHorizontallyLabel
                    )
                }
            },
            trailingContent = {},
            content = {
                FilledIconButton(
                    modifier = Modifier.width(64.dp),
                    onClick = {
                        onAction(
                            ImageCropperAction.ImageCropped(
                                cropController.crop()
                            )
                        )
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.crop),
                        contentDescription = stringResource(R.string.accessibility_crop_button)
                    )
                }
            }
        )
        ImageCropper(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            cropController = cropController
        )

        if (uiState.isImagePickerVisible) {
            SelectOrCaptureImagePicker(
                onGalleryImageObtained = { onAction(ImageCropperAction.GalleryImageObtained(it)) },
                onCameraImageTaken = { onAction(ImageCropperAction.CameraImageTaken(it)) },
                onPermissionLauncherResultReceived = { permissionGranted, launchCamera ->
                    onAction(
                        ImageCropperAction.PermissionLauncherResultReceived(
                            isGranted = permissionGranted,
                            launchCamera = { uri ->
                                launchCamera(
                                    uri
                                )
                            })
                    )
                },
                onDismissRequest = { onAction(ImageCropperAction.ImagePickerDismissed) }
            )
        }

        if (uiState.isPermissionDeniedDialogVisible) {
            CameraPermissionDeniedDialog(
                onDismissRequest = { onAction(ImageCropperAction.PermissionDeniedDialogDismissed) },
                onGoToSystemSettings = {
                    onAction(
                        ImageCropperAction.GoToSystemSettingsClicked
                    )
                })
        }
    }
}