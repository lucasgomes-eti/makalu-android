package eti.lucasgomes.makalu.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem<T : Any>(
    val name: String,
    val route: T,
    val icon: ImageVector,
    val contentDescription: String
)