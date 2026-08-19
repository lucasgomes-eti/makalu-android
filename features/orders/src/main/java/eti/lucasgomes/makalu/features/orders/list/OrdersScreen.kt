package eti.lucasgomes.makalu.features.orders.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.components.OrderStatusTag
import eti.lucasgomes.makalu.features.orders.components.OrderTimeline
import eti.lucasgomes.makalu.shared.model.OrderStatus

@Composable
internal fun OrdersScreen(uiState: OrdersUiState) {
    LazyColumn(
        Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(uiState.orders) {
            Card {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .border(
                                    BorderStroke(1.dp, colorScheme.outline),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(eti.lucasgomes.makalu.components.R.drawable.outline_receipt),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colorScheme.primary
                            )
                            Text("Nº ${it.orderNumber}", style = typography.labelMedium)
                        }

                        Spacer(Modifier.width(8.dp))
                        OrderStatusTag(it.status)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            model = it.storeImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(it.storeName)
                            Text(it.deliveryAddressLine)
                        }
                    }
                    OrderTimeline(modifier = Modifier.fillMaxWidth(), status = OrderStatus.PENDING)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${it.itemsCount} items - delivery ${it.deliveryFee}")
                        Text("Total: ${it.totalPrice}")
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun OrdersPreview() {
    OrdersScreen(
        OrdersUiState(
            listOf(
                OrdersUiState.Item(
                    1,
                    OrdersUiState.Item.Status(OrderStatus.PENDING, UiText.PlainText("Pending")),
                    "123",
                    "Sushi Naka",
                    1,
                    "Rua M",
                    3,
                    "2.45",
                    "24.56",
                    "",
                    ""
                )
            )
        )
    )
}