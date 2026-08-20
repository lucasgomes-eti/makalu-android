package eti.lucasgomes.makalu.features.orders.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.features.orders.components.OrderCard
import eti.lucasgomes.makalu.features.orders.components.OrderCardUiState
import eti.lucasgomes.makalu.features.orders.components.toUiStatus
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OrdersScreen(uiState: OrdersUiState, onAction: (OrdersAction) -> Unit) {
    OnFirstComposition { onAction(OrdersAction.OnInitialFetch) }

    LazyColumn(
        Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (uiState.generalError != UiText.Empty) {
            item {
                ErrorBanner(uiState.generalError.asString()) {
                    onAction(OrdersAction.OnDismissError)
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
        if (uiState.isEmpty) {
            item { EmptyOrders() }
        }
        items(uiState.orders) { order ->
            OrderCard(order.card) { onAction(OrdersAction.OnOrderClicked(order.id)) }
        }
    }
}

@Composable
private fun EmptyOrders() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(eti.lucasgomes.makalu.components.R.drawable.outline_receipt),
            contentDescription = null,
            modifier = Modifier.size(128.dp),
            tint = colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.orders_empty))
    }
}

@Preview(showBackground = true)
@Composable
private fun OrdersPreview() {
    OrdersScreen(
        OrdersUiState(
            orders = listOf(
                OrdersUiState.Item(
                    id = 1,
                    card = OrderCardUiState(
                        orderNumber = "123",
                        status = OrderStatus.IN_ROUTE.toUiStatus(),
                        storeImageUrl = null,
                        storeName = "Sushi Naka",
                        deliveryAddressLine = "Rua M",
                        itemsCount = 3,
                        deliveryFee = BigDecimal("2.45"),
                        totalPrice = BigDecimal("24.56")
                    ),
                    createdAt = Instant.DISTANT_PAST,
                    updatedAt = Instant.DISTANT_PAST
                )
            )
        ),
        onAction = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun OrdersEmptyPreview() {
    OrdersScreen(OrdersUiState(), onAction = {})
}

@Preview(showBackground = true)
@Composable
private fun OrdersErrorPreview() {
    OrdersScreen(
        OrdersUiState(generalError = UiText.PlainText("MK-0 - Unexpected error during request")),
        onAction = {}
    )
}
