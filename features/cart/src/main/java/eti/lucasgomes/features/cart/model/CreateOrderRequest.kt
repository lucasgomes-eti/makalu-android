package eti.lucasgomes.features.cart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    @SerialName("store_id")
    val storeId: Long,

    @SerialName("cart_id")
    val cartId: Long
)
