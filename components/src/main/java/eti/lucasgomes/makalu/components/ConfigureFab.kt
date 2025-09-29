package eti.lucasgomes.makalu.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun ConfigureFab(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit
) {
    val fab = LocalFabUiController.current
    LaunchedEffect(Unit) {
        fab.setAction(FabAction(icon, contentDescription, onClick))
    }
}