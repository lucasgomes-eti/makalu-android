package eti.lucasgomes.makalu.shared.navigation

data class PopUpToOptions(
    val destination: Destination,
    val inclusive: Boolean = false,
    val saveState: Boolean = false
)