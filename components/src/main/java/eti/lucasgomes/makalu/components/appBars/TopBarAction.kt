package eti.lucasgomes.makalu.components.appBars

import androidx.compose.ui.graphics.painter.Painter

data class TopBarAction(
    val icon: Painter,
    val contentDescription: String,
    val onClick: () -> Unit
)