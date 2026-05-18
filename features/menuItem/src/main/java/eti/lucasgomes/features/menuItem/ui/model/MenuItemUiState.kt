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
    val configurations: Map<ConfigurationUiState, List<OptionUiState>> = emptyMap(),
    val notes: String = "",
) {
    val totalPrice: BigDecimal
        get() = configurations.flatMap { (_, options) -> options }.filter { option ->
            when (option) {
                is OptionUiState.SingleChoice -> option.isSelected
                is OptionUiState.MultipleChoice -> option.isSelected
                is OptionUiState.Quantity -> option.amount > 0
            }
        }.map { option ->
            when (option) {
                is OptionUiState.SingleChoice -> option.additionalPrice
                is OptionUiState.MultipleChoice -> option.additionalPrice
                is OptionUiState.Quantity -> option.additionalPrice * option.amount.toBigDecimal()
            }
        }.fold(price) { acc, price -> acc + price }
}

internal sealed class OptionUiState(
    open val id: Long,
    open val label: String,
    open val additionalPrice: BigDecimal
) {
    data class SingleChoice(
        override val id: Long,
        override val label: String = "",
        override val additionalPrice: BigDecimal = BigDecimal.ZERO,
        val isSelected: Boolean = false
    ) : OptionUiState(id, label, additionalPrice)

    data class MultipleChoice(
        override val id: Long,
        override val label: String = "",
        override val additionalPrice: BigDecimal = BigDecimal.ZERO,
        val isSelected: Boolean = false
    ) : OptionUiState(id, label, additionalPrice)

    data class Quantity(
        override val id: Long,
        override val label: String = "",
        override val additionalPrice: BigDecimal = BigDecimal.ZERO,
        val amount: Int = 0
    ) : OptionUiState(id, label, additionalPrice)
}