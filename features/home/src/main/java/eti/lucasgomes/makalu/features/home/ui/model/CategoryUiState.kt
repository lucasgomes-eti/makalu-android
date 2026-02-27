package eti.lucasgomes.makalu.features.home.ui.model

import eti.lucasgomes.makalu.components.dsl.UiText

internal data class CategoryUiState(
    val id: Long,
    val label: UiText,
    val isSelected: Boolean,
)