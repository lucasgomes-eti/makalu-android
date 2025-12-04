package eti.lucasgomes.makalu.components.imagePreview.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropRatio
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.GridLinesType
import com.tanishranjan.cropkit.ImageCropper
import com.tanishranjan.cropkit.rememberCropController
import eti.lucasgomes.makalu.components.R
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.appBars.TopBarAction
import eti.lucasgomes.makalu.components.imagePreview.model.ImagePreviewAction

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ImagePreviewScreen(onAction: (ImagePreviewAction) -> Unit) {
    var imageBitmapState by remember { mutableStateOf<ImageBitmap?>(null) }
    imageBitmapState = ImageBitmap.imageResource(R.drawable.profile_pic)

    ConfigureTopBar(
        title = "Image preview", navigationActions = listOf(
            TopBarAction(
                icon = painterResource(R.drawable.add_photo),
                contentDescription = "Add image icon",
                onClick = {}
            )
        )
    )

    val cropController = rememberCropController(
        bitmap = imageBitmapState!!.asAndroidBitmap(),
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
                AppBarRow {
                    clickableItem(
                        onClick = {
                            cropController.rotateAntiClockwise()
                        },
                        icon = { Icon(painterResource(R.drawable.rotate_left), null) },
                        label = "Rotate left"
                    )
                    clickableItem(
                        onClick = {
                            cropController.rotateClockwise()
                        },
                        icon = { Icon(painterResource(R.drawable.rotate_right), null) },
                        label = "Rotate right"
                    )
                    clickableItem(
                        onClick = {
                            cropController.flipVertically()
                        },
                        icon = { Icon(painterResource(R.drawable.swap_vert), null) },
                        label = "Mirror vertically"
                    )
                    clickableItem(
                        onClick = {
                            cropController.flipHorizontally()
                        },
                        icon = { Icon(painterResource(R.drawable.swap_horiz), null) },
                        label = "Mirror horizontally"
                    )
                }
            },
            trailingContent = {},
            content = {
                FilledIconButton(
                    modifier = Modifier.width(64.dp),
                    onClick = {
                        onAction(
                            ImagePreviewAction.ImageCropped(
                                cropController.crop().asImageBitmap()
                            )
                        )
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.crop),
                        contentDescription = "Localized description"
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
    }
}