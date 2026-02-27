package eti.lucasgomes.makalu.features.home.ui.model

internal data class HomeUiState(
    val address: String = "",
    val categories: List<CategoryUiState> = emptyList(),
    val stores: List<StoreUiState> = emptyList(),
    val isAuthDialogVisible: Boolean = false,
    val isAddressLoading: Boolean = false,
    val isFiltersLoading: Boolean = false,
    val isStoresLoading: Boolean = false,
)