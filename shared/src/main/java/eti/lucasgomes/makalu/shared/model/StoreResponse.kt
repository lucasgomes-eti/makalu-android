package eti.lucasgomes.makalu.shared.model

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
    val coverImageId: Long?
)