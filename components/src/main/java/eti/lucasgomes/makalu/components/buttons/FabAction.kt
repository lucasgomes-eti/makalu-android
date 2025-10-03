package eti.lucasgomes.makalu.components.buttons

import androidx.compose.ui.graphics.painter.Painter

data class FabAction(
    val icon: Painter,
    val contentDescription: String,
    val onClick: () -> Unit
)