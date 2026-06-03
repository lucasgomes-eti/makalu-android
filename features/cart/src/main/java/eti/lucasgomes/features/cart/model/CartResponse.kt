package eti.lucasgomes.features.cart.model

import eti.lucasgomes.makalu.shared.serializer.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CartResponse(
    val id: Long,

    @SerialName("delivery_address_line")
    val deliveryAddressLine: String?,

    val items: List<CartItemResponse>,
    val total: Total
) {
    @Serializable
    data class CartItemResponse(
        val id: Long,

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
            @SerialName("configuration_id")
            val configurationId: Long,
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
