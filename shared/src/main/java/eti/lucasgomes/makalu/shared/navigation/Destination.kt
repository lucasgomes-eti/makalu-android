package eti.lucasgomes.makalu.shared.navigation

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

        @Serializable
        data object Orders : Destination

        @Serializable
        data object Profile : Destination

        @Serializable
        data class ImagePreview(val uri: String) : Destination

        @Serializable
        data class Address(val id: Long? = null) : Destination

        @Serializable
        data class Store(val id: Long) : Destination

        @Serializable
        data class MenuItem(val storeId: Long, val menuItemId: Long) : Destination

        @Serializable
        data class Cart(val storeId: Long) : Destination
    }
}