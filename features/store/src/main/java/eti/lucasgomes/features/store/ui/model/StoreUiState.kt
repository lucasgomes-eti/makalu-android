package eti.lucasgomes.features.store.ui.model

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.mapImageUrl
import java.math.BigDecimal

internal data class StoreUiState(
    val id: Long,
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty,
    val name: UiText = UiText.Empty,
    private val coverImageId: Long? = null,
    val menuItems: Map<String, List<MenuItemUiState>> = emptyMap(),
    val deliveryFee: BigDecimal = BigDecimal.ZERO
) {
    val coverImageUrl: String?
        get() = mapImageUrl(coverImageId)
}
