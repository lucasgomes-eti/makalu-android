package eti.lucasgomes.makalu.features.orders.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.ItemCard
import eti.lucasgomes.makalu.components.ItemConfigurationUiState
import eti.lucasgomes.makalu.components.ItemOptionUiState
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.appBars.LocalTopBarUiController
import eti.lucasgomes.makalu.components.appBars.TopBarUiController
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.buttons.FabUiController
import eti.lucasgomes.makalu.components.buttons.LocalFabUiController
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.features.orders.components.OrderCard
import eti.lucasgomes.makalu.features.orders.components.OrderCardUiState
import eti.lucasgomes.makalu.features.orders.components.toUiStatus
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OrderDetailScreen(
    uiState: OrderDetailUiState,
    onAction: (OrderDetailAction) -> Unit
) {
    OnFirstComposition { onAction(OrderDetailAction.OnInitialFetch) }

    ConfigureTopBar(
        uiState.card?.let { stringResource(R.string.order_detail_title, it.orderNumber) }
            ?: stringResource(R.string.order_detail_title_empty)
    )

    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (uiState.generalError != UiText.Empty) {
            item {
                ErrorBanner(uiState.generalError.asString()) {
                    onAction(OrderDetailAction.OnDismissError)
                }
            }
        }
        if (uiState.isLoading) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) { LoadingIndicator() }
            }
        }
        uiState.card?.let { card -> item { OrderCard(card) } }
        if (uiState.items.isNotEmpty()) {
            item { Text(stringResource(R.string.order_detail_items), style = typography.titleLarge) }
            items(uiState.items) {
                ItemCard(it.name, it.imageUrl, it.price, it.notes, it.configurations)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderDetailPreview() {
    CompositionLocalProvider(LocalTopBarUiController provides TopBarUiController()) {
        OrderDetailScreen(
            OrderDetailUiState(
                id = 1,
                card = OrderCardUiState(
                    orderNumber = "123",
                    status = OrderStatus.IN_ROUTE.toUiStatus(),
                    storeImageUrl = null,
                    storeName = "Sushi Naka",
                    deliveryAddressLine = "Rua M",
                    itemsCount = 1,
                    deliveryFee = BigDecimal("2.45"),
                    totalPrice = BigDecimal("24.56")
                ),
                items = listOf(
                    OrderDetailUiState.Item(
                        id = 1,
                        name = "Hot Filadélfia",
                        imageUrl = null,
                        price = BigDecimal("22.11"),
                        notes = "No onions",
                        configurations = listOf(
                            ItemConfigurationUiState(
                                id = 1,
                                name = "Sauces",
                                options = listOf(
                                    ItemOptionUiState(
                                        id = 1,
                                        name = "Teriyaki",
                                        additionalPrice = BigDecimal("1.50"),
                                        quantity = 2
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderDetailLoadingPreview() {
    CompositionLocalProvider(LocalTopBarUiController provides TopBarUiController()) {
        OrderDetailScreen(OrderDetailUiState(id = 1, isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderDetailErrorPreview() {
    CompositionLocalProvider(LocalTopBarUiController provides TopBarUiController()) {
        OrderDetailScreen(
            OrderDetailUiState(
                id = 1,
                generalError = UiText.PlainText("MK-0 - Unexpected error during request")
            ),
            onAction = {}
        )
    }
}
