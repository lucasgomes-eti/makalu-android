package eti.lucasgomes.features.menuItem.ui.model

import eti.lucasgomes.makalu.components.dsl.UiText
import java.math.BigDecimal

internal data class MenuItemUiState(
    val id: Long,
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val imageUrl: String? = null,
    val name: UiText = UiText.Empty,
    val ingredients: String? = null,
    val price: BigDecimal = BigDecimal.ZERO,
    val configurations: Map<ConfigurationUiState, List<String>> = emptyMap()
)
