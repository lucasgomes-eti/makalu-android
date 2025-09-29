package eti.lucasgomes.makalu.ui.appScaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.components.FabUiController

@Composable
fun FloatingActionButton(fabUiController: FabUiController) {
    AnimatedVisibility(fabUiController.isFabVisible.value) {
        FloatingActionButton(onClick = { fabUiController.action.value?.onClick?.invoke() }) {
            fabUiController.action.value?.let { action ->
                Icon(action.icon, action.contentDescription)
            }
        }
    }
}