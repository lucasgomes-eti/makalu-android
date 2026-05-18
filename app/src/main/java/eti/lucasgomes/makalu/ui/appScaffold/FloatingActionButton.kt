package eti.lucasgomes.makalu.ui.appScaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.buttons.FabUiController

@Composable
fun FloatingActionButton(fabUiController: FabUiController) {
    AnimatedVisibility(fabUiController.isFabVisible.value) {
        if (fabUiController.isExtended.value) {
            ExtendedFloatingActionButton(
                onClick = { fabUiController.action.value?.onClick?.invoke() },
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
            FloatingActionButton(onClick = { fabUiController.action.value?.onClick?.invoke() }) {
                fabUiController.action.value?.let { action ->
                    Icon(action.icon, action.contentDescription)
                }
            }
        }

    }
}