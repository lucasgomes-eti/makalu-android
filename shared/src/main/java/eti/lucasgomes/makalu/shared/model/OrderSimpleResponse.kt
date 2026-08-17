package eti.lucasgomes.makalu.shared.model

import eti.lucasgomes.makalu.shared.serializer.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import kotlin.time.Instant

@Serializable
data class OrderSimpleResponse(
    val id: Long,
    val status: OrderStatus,

    @SerialName("order_number")
    val orderNumber: String,

    @SerialName("store_name")
    val storeName: String,

    @SerialName("store_image_id")
    val storeImageId: Long?,

    @SerialName("delivery_address_line")
    val deliveryAddressLine: String,

    @SerialName("items_count")
    val itemsCount: Int,

    val total: Total,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("updated_at")
    val updatedAt: Instant
) {
    @Serializable
    data class Total(

        @SerialName("delivery_fee")
        @Serializable(with = BigDecimalSerializer::class)
        val deliveryFee: BigDecimal,

        @Serializable(with = BigDecimalSerializer::class)
        val total: BigDecimal
    )
}