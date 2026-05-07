package eti.lucasgomes.features.menuItem.model

import eti.lucasgomes.makalu.shared.serializer.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class MenuItemResponse(
    val id: Long,

    @SerialName("store_id")
    val storeId: Long,

    val category: String,
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val ingredients: String?,
    val configurations: List<Configuration>,

    @SerialName("image_id")
    val imageId: Long?
) {
    @Serializable
    data class Configuration(
        val name: String,
        val type: Type,
        val options: List<Option>
    ) {
        enum class Type { SINGLE_CHOICE, MULTIPLE_CHOICE, QUANTITY }

        @Serializable
        data class Option(
            val name: String,
            @Serializable(with = BigDecimalSerializer::class)
            val additionalPrice: BigDecimal
        )
    }
}