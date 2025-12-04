package eti.lucasgomes.makalu.components.pickers

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectOrCaptureImagePicker(
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