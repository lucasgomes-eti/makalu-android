package eti.lucasgomes.features.store.ui.model

import eti.lucasgomes.features.store.model.MenuItemResponse
import eti.lucasgomes.makalu.components.dsl.UiText

internal data class StoreUiState(
    val id: Long,
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val name: UiText = UiText.Empty,
    val menuItems: Map<String, List<MenuItemResponse>> = emptyMap()
)
