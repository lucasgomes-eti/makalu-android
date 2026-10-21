package eti.lucasgomes.makalu.components

import java.math.BigDecimal

data class ItemConfigurationUiState(
    val id: Long,
    val name: String,
    val options: List<ItemOptionUiState>
)

data class ItemOptionUiState(
    val id: Long,
    val name: String,
    val additionalPrice: BigDecimal,
    val quantity: Int
)
