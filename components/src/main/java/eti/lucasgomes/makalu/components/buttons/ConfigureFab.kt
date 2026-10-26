package eti.lucasgomes.makalu.components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun ConfigureFab(
    icon: Painter,
    contentDescription: String,
    isExtended: Boolean = false,
    label: String = "",
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val fab = LocalFabUiController.current
    LaunchedEffect(icon, isExtended, label, isEnabled) {
        fab.setAction(FabAction(icon, contentDescription, onClick))
        fab.setExtended(isExtended)
        fab.setLabel(label)
        fab.setEnabled(isEnabled)
    }
}
