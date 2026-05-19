package eti.lucasgomes.features.store.ui.model

import eti.lucasgomes.makalu.shared.mapImageUrl
import java.math.BigDecimal

data class MenuItemUiState(
    val id: Long,
    val category: String = "",
    val name: String = "",
    val price: BigDecimal = BigDecimal.ZERO,
    private val imageId: Long? = null
) {
    val imageUrl: String?
        get() = mapImageUrl(imageId)
}
