package eti.lucasgomes.makalu.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {

    object Graph {
        @Serializable
        data object Home : Destination

        @Serializable
        data object Auth : Destination
    }

    object Screen {
        @Serializable
        data object Home : Destination

        @Serializable
        data object Login : Destination

        @Serializable
        data object Registration : Destination
    }
}