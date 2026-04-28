package eti.lucasgomes.features.store.ui.model

import java.math.BigDecimal

data class MenuItemUiState(
    val id: Long,
    val category: String = "",
    val name: String = "",
    val price: BigDecimal = BigDecimal.ZERO,
    val imageUrl: String? = null
)
