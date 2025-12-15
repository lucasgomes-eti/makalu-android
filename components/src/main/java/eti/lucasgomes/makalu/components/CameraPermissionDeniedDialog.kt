package eti.lucasgomes.makalu.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun CameraPermissionDeniedDialog(onDismissRequest: () -> Unit, onGoToSystemSettings: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(painterResource(R.drawable.close), null)
        },
        title = {
            Text(stringResource(R.string.camera_permission_denied))
        },
        text = {
            Text(stringResource(R.string.please_grant_the_camera_permission))
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onGoToSystemSettings) {
                Text(stringResource(R.string.go_to_system_settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }
    )
}