package eti.lucasgomes.makalu.features.home.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import eti.lucasgomes.makalu.components.ExpressiveTextButton
import eti.lucasgomes.makalu.features.home.R

@Composable
internal fun RequireAuthDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(painterResource(R.drawable.login), "Login Icon") },
        title = { Text("Login Required") },
        text = { Text("You must be logged in to order.") },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ExpressiveTextButton(onClick = onConfirmation) {
                Text("Login")
            }
        },
        dismissButton = {
            ExpressiveTextButton(onClick = onDismissRequest) {
                Text(
                    "Dismiss",
                    color = colorScheme.contentColorFor(AlertDialogDefaults.containerColor)
                )
            }
        }
    )
}