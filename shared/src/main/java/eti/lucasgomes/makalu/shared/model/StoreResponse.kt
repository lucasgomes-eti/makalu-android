package eti.lucasgomes.makalu.shared.model

import eti.lucasgomes.makalu.shared.serializer.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreResponse(
    val id: Long,
    val name: String,
    val categories: List<CategoryResponse>,

    @SerialName("logo_image_id")
    val logoImageId: Long?,

    @SerialName("cover_image_id")
    val coverImageId: Long?,

    @SerialName("delivery_fee")
    @Serializable(with = BigDecimalSerializer::class)
    val deliveryFee: BigDecimal
)
