package eti.lucasgomes.makalu.features.orders.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.DairyCream
import eti.lucasgomes.makalu.components.GreenPea
import eti.lucasgomes.makalu.components.HawkesBlue
import eti.lucasgomes.makalu.components.Himalaya
import eti.lucasgomes.makalu.components.ScienceBlue
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.features.orders.components.Style.Companion.getStyleFromStatus
import eti.lucasgomes.makalu.features.orders.list.OrdersUiState
import eti.lucasgomes.makalu.shared.model.OrderStatus

@Composable
internal fun OrderStatusTag(status: OrdersUiState.Item.Status) {
    val style = getStyleFromStatus(status.data)
    Row(
        modifier = Modifier
            .background(
                style.backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(style.iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = style.contentColor
        )
        Text(status.text.asString(), color = style.contentColor)
    }
}

private class Style private constructor(
    val backgroundColor: Color,
    val contentColor: Color,
    @DrawableRes val iconRes: Int
) {
    companion object {
        @Composable
        fun getStyleFromStatus(status: OrderStatus): Style = when (status) {
            OrderStatus.PENDING -> Style(
                backgroundColor = DairyCream,
                contentColor = Himalaya,
                iconRes = R.drawable.schedule
            )

            OrderStatus.ACCEPTED -> Style(
                backgroundColor = HawkesBlue,
                contentColor = ScienceBlue,
                iconRes = R.drawable.restaurant
            )
            OrderStatus.CANCELLED -> Style(
                backgroundColor = colorScheme.error,
                contentColor = Color.White,
                iconRes = eti.lucasgomes.makalu.components.R.drawable.close
            )
            OrderStatus.IN_ROUTE -> Style(
                backgroundColor = colorScheme.primaryContainer,
                contentColor = colorScheme.primary,
                iconRes = R.drawable.two_wheeler
            )
            OrderStatus.FINISHED -> Style(
                backgroundColor = GreenPea,
                contentColor = Color.White,
                iconRes = R.drawable.check_circle
            )
        }
    }
}

@Preview
@Composable
private fun OrderStatusTagPendingPreview() {
    OrderStatusTag(status = OrdersUiState.Item.Status(OrderStatus.PENDING, UiText.PlainText("Pending")))
}

@Preview
@Composable
private fun OrderStatusTagAcceptedPreview() {
    OrderStatusTag(status = OrdersUiState.Item.Status(OrderStatus.ACCEPTED, UiText.PlainText("Accepted")))
}

@Preview
@Composable
private fun OrderStatusTagCanceledPreview() {
    OrderStatusTag(status = OrdersUiState.Item.Status(OrderStatus.CANCELLED, UiText.PlainText("Cancelled")))
}

@Preview
@Composable
private fun OrderStatusTagInRoutePreview() {
    OrderStatusTag(status = OrdersUiState.Item.Status(OrderStatus.IN_ROUTE, UiText.PlainText("In route")))
}

@Preview
@Composable
private fun OrderStatusTagFinishedPreview() {
    OrderStatusTag(status = OrdersUiState.Item.Status(OrderStatus.FINISHED, UiText.PlainText("Finished")))
}