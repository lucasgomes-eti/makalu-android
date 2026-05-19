package eti.lucasgomes.features.menuItem.ui.model

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.mapImageUrl
import java.math.BigDecimal

internal data class MenuItemUiState(
    val id: Long,
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    private val imageId: Long? = null,
    val name: UiText = UiText.Empty,
    val ingredients: String? = null,
    val price: BigDecimal = BigDecimal.ZERO,
    val configurations: Map<ConfigurationUiState, List<OptionUiState>> = emptyMap(),
    val notes: String = "",
) {

    val imageUrl: String?
        get() = mapImageUrl(imageId)

    val totalPrice: BigDecimal
        get() = configurations.flatMap { (_, options) -> options }
            .filter { it.isSelected }
            .map { it.totalPrice }
            .fold(price) { acc, price -> acc + price }
}

internal sealed class OptionUiState(
    open val id: Long,
    open val label: String,
    open val additionalPrice: BigDecimal,
) {

    abstract val isSelected: Boolean
    abstract val totalPrice: BigDecimal

    data class SingleChoice(
        override val id: Long,
        override val label: String = "",
        override val additionalPrice: BigDecimal = BigDecimal.ZERO,
        override val isSelected: Boolean = false
    ) : OptionUiState(id, label, additionalPrice) {
        override val totalPrice: BigDecimal
            get() = additionalPrice
    }

    data class MultipleChoice(
        override val id: Long,
        override val label: String = "",
        override val additionalPrice: BigDecimal = BigDecimal.ZERO,
        override val isSelected: Boolean = false
    ) : OptionUiState(id, label, additionalPrice) {
        override val totalPrice: BigDecimal
            get() = additionalPrice
    }

    data class Quantity(
        override val id: Long,
        override val label: String = "",
        override val additionalPrice: BigDecimal = BigDecimal.ZERO,
        val amount: Int = 0,
    ) : OptionUiState(id, label, additionalPrice) {

        override val isSelected: Boolean
            get() = amount > 0

        override val totalPrice: BigDecimal
            get() = additionalPrice * amount.toBigDecimal()
    }
}