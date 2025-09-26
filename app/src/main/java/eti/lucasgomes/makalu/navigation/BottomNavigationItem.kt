package eti.lucasgomes.makalu.navigation

import androidx.compose.ui.graphics.painter.Painter

data class BottomNavigationItem<T : Any>(
    val name: String,
    val route: T,
    val defaultIcon: Painter,
    val selectedIcon: Painter = defaultIcon,
    val contentDescription: String
)