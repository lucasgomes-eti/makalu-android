package eti.lucasgomes.makalu.features.orders.detail

import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar

@Composable
internal fun OrderDetailScreen(uiState: OrderDetailUiState) {
    ConfigureTopBar("Order #${uiState.id}")
}