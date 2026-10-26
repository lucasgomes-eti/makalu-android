package eti.lucasgomes.makalu.ui.appScaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.buttons.FabUiController

@Composable
fun FloatingActionButton(fabUiController: FabUiController) {
    val isEnabled = fabUiController.isEnabled.value
    val onClick = { if (isEnabled) fabUiController.action.value?.onClick?.invoke() }
    val containerColor =
        if (isEnabled) {
            FloatingActionButtonDefaults.containerColor
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        }
    val contentColor =
        if (isEnabled) {
            contentColorFor(FloatingActionButtonDefaults.containerColor)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }
    val elevation =
        if (isEnabled) {
            FloatingActionButtonDefaults.elevation()
        } else {
            FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
        }
    val modifier = Modifier.semantics { if (!isEnabled) disabled() }

    AnimatedVisibility(fabUiController.isFabVisible.value) {
        if (fabUiController.isExtended.value) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = elevation
            ) {
                fabUiController.action.value?.let { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(action.icon, action.contentDescription)
                        Text(fabUiController.label.value)
                    }
                }
            }
        } else {
            FloatingActionButton(
                onClick = onClick,
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = elevation
            ) {
                fabUiController.action.value?.let { action ->
                    Icon(action.icon, action.contentDescription)
                }
            }
        }
    }
}
