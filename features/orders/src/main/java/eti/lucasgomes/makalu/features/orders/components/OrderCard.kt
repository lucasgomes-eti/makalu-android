package eti.lucasgomes.makalu.features.orders.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal

@Composable
internal fun OrderCard(uiState: OrderCardUiState, onClick: (() -> Unit)? = null) {
    if (onClick == null) {
        Card { OrderCardContent(uiState) }
    } else {
        Card(onClick = onClick) { OrderCardContent(uiState) }
    }
}

@Composable
private fun OrderCardContent(uiState: OrderCardUiState) {
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
                Text("Nº ${uiState.orderNumber}", style = typography.labelMedium)
            }

            Spacer(Modifier.width(8.dp))
            OrderStatusTag(uiState.status)
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                model = uiState.storeImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(uiState.storeName)
                Text(uiState.deliveryAddressLine)
            }
        }
        OrderTimeline(modifier = Modifier.fillMaxWidth(), status = uiState.status.data)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currency =
                stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)
            Text("${uiState.itemsCount} items - delivery $currency ${uiState.deliveryFee}")
            Text("Total: $currency ${uiState.totalPrice}")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderCardPreview() {
    OrderCard(
        OrderCardUiState(
            orderNumber = "123",
            status = OrderStatusUiState(OrderStatus.IN_ROUTE, UiText.PlainText("In route")),
            storeImageUrl = null,
            storeName = "Sushi Naka",
            deliveryAddressLine = "Rua M",
            itemsCount = 3,
            deliveryFee = BigDecimal("2.45"),
            totalPrice = BigDecimal("24.56")
        )
    )
}
