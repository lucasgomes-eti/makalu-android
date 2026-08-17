package eti.lucasgomes.makalu.features.orders

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
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
import eti.lucasgomes.makalu.shared.model.OrderStatus

@Composable
internal fun OrderTimeline(
    modifier: Modifier = Modifier,
    status: OrderStatus
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(getIconRes(OrderStatus.PENDING, status)),
            contentDescription = null,
            tint = getIconTint(OrderStatus.PENDING, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(OrderStatus.PENDING, status),
                    shape = CircleShape
                )
                .padding(4.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 2.dp,
            color = getDividerColor(status, 0)
        )
        Icon(
            painter = painterResource(getIconRes(OrderStatus.ACCEPTED, status)),
            contentDescription = null,
            tint = getIconTint(OrderStatus.ACCEPTED, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(OrderStatus.ACCEPTED, status),
                    shape = CircleShape
                )
                .padding(4.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 2.dp,
            getDividerColor(status, 1)
        )
        Icon(
            painter = painterResource(getIconRes(OrderStatus.IN_ROUTE, status)),
            contentDescription = null,
            tint = getIconTint(OrderStatus.IN_ROUTE, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(OrderStatus.IN_ROUTE, status),
                    shape = CircleShape
                )
                .padding(4.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 2.dp,
            getDividerColor(status, 2)
        )
        Icon(
            painter = painterResource(getIconRes(OrderStatus.FINISHED, status)),
            contentDescription = null,
            tint = getIconTint(OrderStatus.FINISHED, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(OrderStatus.FINISHED, status),
                    shape = CircleShape
                )
                .padding(4.dp)
        )
    }
}

@Composable
private fun getStepColor(forStatus: OrderStatus, withStatus: OrderStatus): Color {
    when (forStatus) {
        OrderStatus.PENDING -> {
            if (withStatus == OrderStatus.PENDING) return Himalaya
            if (withStatus > OrderStatus.PENDING) return DairyCream
            return colorScheme.surfaceDim
        }

        OrderStatus.ACCEPTED, OrderStatus.CANCELLED -> {
            if (withStatus == OrderStatus.ACCEPTED) return ScienceBlue
            if (withStatus == OrderStatus.CANCELLED) return colorScheme.error
            if (withStatus > OrderStatus.ACCEPTED) return HawkesBlue
            return colorScheme.surfaceDim
        }

        OrderStatus.IN_ROUTE -> {
            if (withStatus == OrderStatus.IN_ROUTE) return colorScheme.primary
            if (withStatus > OrderStatus.IN_ROUTE) return colorScheme.primaryContainer
            return colorScheme.surfaceDim
        }

        OrderStatus.FINISHED -> {
            if (withStatus == OrderStatus.FINISHED) return GreenPea
            if (withStatus > OrderStatus.FINISHED) return GreenPea
            return colorScheme.surfaceDim
        }
    }
}

@DrawableRes
private fun getIconRes(forStatus: OrderStatus, withStatus: OrderStatus): Int {
    when (forStatus) {
        OrderStatus.PENDING -> {
            return if (withStatus > OrderStatus.PENDING) R.drawable.check
            else R.drawable.schedule
        }

        OrderStatus.ACCEPTED, OrderStatus.CANCELLED -> {
            return if (withStatus == OrderStatus.CANCELLED) eti.lucasgomes.makalu.components.R.drawable.close
            else if (withStatus > OrderStatus.ACCEPTED) R.drawable.check
            else R.drawable.restaurant
        }

        OrderStatus.IN_ROUTE -> {
            return if (withStatus > OrderStatus.IN_ROUTE) R.drawable.check
            else R.drawable.two_wheeler
        }

        OrderStatus.FINISHED -> {
            return if (withStatus > OrderStatus.FINISHED) R.drawable.check
            else R.drawable.check_circle
        }
    }
}

@Composable
private fun getIconTint(forStatus: OrderStatus, withStatus: OrderStatus): Color {
    when (forStatus) {
        OrderStatus.PENDING -> {
            if (withStatus == OrderStatus.PENDING) return Color.White
            if (withStatus > OrderStatus.PENDING) return Himalaya
            return colorScheme.inverseOnSurface
        }

        OrderStatus.ACCEPTED, OrderStatus.CANCELLED -> {
            if (withStatus == OrderStatus.ACCEPTED) return Color.White
            if (withStatus == OrderStatus.CANCELLED) return Color.White
            if (withStatus > OrderStatus.ACCEPTED) return ScienceBlue
            return colorScheme.inverseOnSurface
        }

        OrderStatus.IN_ROUTE -> {
            if (withStatus == OrderStatus.IN_ROUTE) return Color.White
            if (withStatus > OrderStatus.IN_ROUTE) return colorScheme.primary
            return colorScheme.inverseOnSurface
        }

        OrderStatus.FINISHED -> {
            if (withStatus == OrderStatus.FINISHED) return Color.White
            if (withStatus > OrderStatus.FINISHED) return Color.White
            return colorScheme.inverseOnSurface
        }
    }
}

@Composable
private fun getDividerColor(forStatus: OrderStatus, atIndex: Int): Color {
    when (forStatus) {
        OrderStatus.PENDING -> {
            return colorScheme.outlineVariant
        }

        OrderStatus.ACCEPTED -> {
            if (atIndex == 0) {
                return ScienceBlue
            }

            return colorScheme.outlineVariant
        }

        OrderStatus.CANCELLED -> {
            if (atIndex == 0) {
                return colorScheme.error
            }

            return colorScheme.outlineVariant
        }

        OrderStatus.IN_ROUTE -> {
            if (atIndex <= 1) {
                return colorScheme.primary
            }
            return colorScheme.outlineVariant
        }

        OrderStatus.FINISHED -> {
            return GreenPea
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelinePendingPreview() {
    OrderTimeline(status = OrderStatus.PENDING)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineAcceptedPreview() {
    OrderTimeline(status = OrderStatus.ACCEPTED)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineCanceledPreview() {
    OrderTimeline(status = OrderStatus.CANCELLED)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineInRoutePreview() {
    OrderTimeline(status = OrderStatus.IN_ROUTE)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineFinishedPreview() {
    OrderTimeline(status = OrderStatus.FINISHED)
}