package eti.lucasgomes.features.menuItem.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemRequest(
    val configurations: List<Configuration>?,
    val notes: String? = null
) {
    @Serializable
    data class Configuration(
        @SerialName("menu_item_configuration_option_id")
        val menuItemConfigurationOptionId: Long?,

        val quantity: Int? = 1
    )
}
