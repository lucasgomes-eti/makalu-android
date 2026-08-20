package eti.lucasgomes.makalu.features.orders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.model.OrderStatus

@Composable
internal fun OrderStatusTag(status: OrderStatusUiState) {
    val palette = status.data.palette()
    Row(
        modifier = Modifier
            .background(
                palette.soft,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(palette.icon),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = palette.onSoft
        )
        Text(status.text.asString(), color = palette.onSoft)
    }
}

@Preview
@Composable
private fun OrderStatusTagPendingPreview() {
    OrderStatusTag(status = OrderStatusUiState(OrderStatus.PENDING, UiText.PlainText("Pending")))
}

@Preview
@Composable
private fun OrderStatusTagAcceptedPreview() {
    OrderStatusTag(status = OrderStatusUiState(OrderStatus.ACCEPTED, UiText.PlainText("Accepted")))
}

@Preview
@Composable
private fun OrderStatusTagCanceledPreview() {
    OrderStatusTag(status = OrderStatusUiState(OrderStatus.CANCELLED, UiText.PlainText("Cancelled")))
}

@Preview
@Composable
private fun OrderStatusTagInRoutePreview() {
    OrderStatusTag(status = OrderStatusUiState(OrderStatus.IN_ROUTE, UiText.PlainText("In route")))
}

@Preview
@Composable
private fun OrderStatusTagFinishedPreview() {
    OrderStatusTag(status = OrderStatusUiState(OrderStatus.FINISHED, UiText.PlainText("Finished")))
}
