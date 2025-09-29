package eti.lucasgomes.makalu.features.home.ui.model

internal data class HomeUiState(
    val address: String = "",
    val categories: List<CategoryUiState> = emptyList(),
    val stores: List<StoreUiState> = emptyList(),
    val isAuthDialogVisible: Boolean = false
)