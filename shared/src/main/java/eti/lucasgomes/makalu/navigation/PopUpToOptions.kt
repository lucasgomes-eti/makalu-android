package eti.lucasgomes.makalu.navigation

data class PopUpToOptions(
    val destination: Destination,
    val inclusive: Boolean = false,
    val saveState: Boolean = false
)