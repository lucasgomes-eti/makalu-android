package eti.lucasgomes.features.store.ui.model

import eti.lucasgomes.makalu.components.dsl.UiText

internal data class StoreUiState(
    val id: Long,
    val name: UiText = UiText.Empty,
)
