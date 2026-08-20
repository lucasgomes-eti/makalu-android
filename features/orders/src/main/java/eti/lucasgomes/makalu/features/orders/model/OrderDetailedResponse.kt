package eti.lucasgomes.makalu.features.orders.model

import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.serializer.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import kotlin.time.Instant

@Serializable
data class OrderDetailedResponse(
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

    val items: List<OrderItemResponse>,
    val total: Total,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("updated_at")
    val updatedAt: Instant
) {
    @Serializable
    data class OrderItemResponse(
        val id: Long,

        @SerialName("menu_item_id")
        val menuItemId: Long,

        @SerialName("image_id")
        val imageId: Long?,

        val name: String,

        @Serializable(with = BigDecimalSerializer::class)
        val price: BigDecimal,

        val notes: String?,
        val configurations: List<Configuration>
    ) {
        @Serializable
        data class Configuration(
            val id: Long,
            val name: String,
            val options: List<Option>
        ) {
            @Serializable
            data class Option(
                val id: Long,
                val name: String,

                @SerialName("additional_price")
                @Serializable(with = BigDecimalSerializer::class)
                val additionalPrice: BigDecimal,

                val quantity: Int
            )
        }
    }

    @Serializable
    data class Total(

        @SerialName("delivery_fee")
        @Serializable(with = BigDecimalSerializer::class)
        val deliveryFee: BigDecimal,

        @Serializable(with = BigDecimalSerializer::class)
        val total: BigDecimal
    )
}
