package eti.lucasgomes.makalu.features.home.ui.model

import eti.lucasgomes.makalu.components.dsl.UiText

internal data class HomeUiState(
    val address: UiText = UiText.Empty,
    val categories: List<CategoryUiState> = emptyList(),
    val stores: List<StoreUiState> = emptyList(),
    val isAuthDialogVisible: Boolean = false,
    val isAddressLoading: Boolean = false,
    val isFiltersLoading: Boolean = false,
    val isStoresLoading: Boolean = false,
) {
    val selectedCategoriesIds: List<Long>
        get() = categories.filter { it.isSelected }.map { it.id }
}