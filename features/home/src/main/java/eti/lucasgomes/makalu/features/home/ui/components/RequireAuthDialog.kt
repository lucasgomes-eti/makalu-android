package eti.lucasgomes.makalu.features.home.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import eti.lucasgomes.makalu.components.buttons.ExpressiveTextButton
import eti.lucasgomes.makalu.features.home.R

@Composable
internal fun RequireAuthDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                painterResource(R.drawable.login),
                stringResource(R.string.accessibility_login_icon)
            )
        },
        title = { Text(stringResource(R.string.login_required)) },
        text = { Text(stringResource(R.string.you_must_be_logged_in_to_order)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ExpressiveTextButton(onClick = onConfirmation) {
                Text(stringResource(R.string.login))
            }
        },
        dismissButton = {
            ExpressiveTextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.dismiss),
                    color = colorScheme.contentColorFor(AlertDialogDefaults.containerColor)
                )
            }
        }
    )
}