package eti.lucasgomes.makalu.features.home.ui.model

data class HomeUiState(
    val address: String = "",
    val categories: List<CategoryUiState> = emptyList(),
    val stores: List<StoreUiState> = emptyList()
)