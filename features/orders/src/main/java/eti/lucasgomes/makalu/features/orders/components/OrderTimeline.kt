package eti.lucasgomes.makalu.features.orders.components

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
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.model.OrderStatus.ACCEPTED
import eti.lucasgomes.makalu.shared.model.OrderStatus.FINISHED
import eti.lucasgomes.makalu.shared.model.OrderStatus.IN_ROUTE
import eti.lucasgomes.makalu.shared.model.OrderStatus.PENDING

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
            painter = painterResource(getIconRes(PENDING, status)),
            contentDescription = null,
            tint = getIconTint(PENDING, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(PENDING, status),
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
            painter = painterResource(getIconRes(ACCEPTED, status)),
            contentDescription = null,
            tint = getIconTint(ACCEPTED, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(ACCEPTED, status),
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
            painter = painterResource(getIconRes(IN_ROUTE, status)),
            contentDescription = null,
            tint = getIconTint(IN_ROUTE, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(IN_ROUTE, status),
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
            painter = painterResource(getIconRes(FINISHED, status)),
            contentDescription = null,
            tint = getIconTint(FINISHED, status),
            modifier = Modifier
                .size(28.dp)
                .background(
                    getStepColor(FINISHED, status),
                    shape = CircleShape
                )
                .padding(4.dp)
        )
    }
}

@Composable
private fun getStepColor(forStatus: OrderStatus, withStatus: OrderStatus): Color {
    when (forStatus) {
        PENDING -> {
            if (withStatus == PENDING) return Himalaya
            if (withStatus > PENDING) return DairyCream
            return colorScheme.surfaceDim
        }

        ACCEPTED, OrderStatus.CANCELLED -> {
            if (withStatus == ACCEPTED) return ScienceBlue
            if (withStatus == OrderStatus.CANCELLED) return colorScheme.error
            if (withStatus > ACCEPTED) return HawkesBlue
            return colorScheme.surfaceDim
        }

        IN_ROUTE -> {
            if (withStatus == IN_ROUTE) return colorScheme.primary
            if (withStatus > IN_ROUTE) return colorScheme.primaryContainer
            return colorScheme.surfaceDim
        }

        FINISHED -> {
            if (withStatus == FINISHED) return GreenPea
            if (withStatus > FINISHED) return GreenPea
            return colorScheme.surfaceDim
        }
    }
}

@DrawableRes
private fun getIconRes(forStatus: OrderStatus, withStatus: OrderStatus): Int {
    when (forStatus) {
        PENDING -> {
            return if (withStatus > PENDING) R.drawable.check
            else R.drawable.schedule
        }

        ACCEPTED, OrderStatus.CANCELLED -> {
            return if (withStatus == OrderStatus.CANCELLED) eti.lucasgomes.makalu.components.R.drawable.close
            else if (withStatus > ACCEPTED) R.drawable.check
            else R.drawable.restaurant
        }

        IN_ROUTE -> {
            return if (withStatus > IN_ROUTE) R.drawable.check
            else R.drawable.two_wheeler
        }

        FINISHED -> {
            return if (withStatus > FINISHED) R.drawable.check
            else R.drawable.check_circle
        }
    }
}

@Composable
private fun getIconTint(forStatus: OrderStatus, withStatus: OrderStatus): Color {
    when (forStatus) {
        PENDING -> {
            if (withStatus == PENDING) return Color.White
            if (withStatus > PENDING) return Himalaya
            return colorScheme.inverseOnSurface
        }

        ACCEPTED, OrderStatus.CANCELLED -> {
            if (withStatus == ACCEPTED) return Color.White
            if (withStatus == OrderStatus.CANCELLED) return Color.White
            if (withStatus > ACCEPTED) return ScienceBlue
            return colorScheme.inverseOnSurface
        }

        IN_ROUTE -> {
            if (withStatus == IN_ROUTE) return Color.White
            if (withStatus > IN_ROUTE) return colorScheme.primary
            return colorScheme.inverseOnSurface
        }

        FINISHED -> {
            if (withStatus == FINISHED) return Color.White
            if (withStatus > FINISHED) return Color.White
            return colorScheme.inverseOnSurface
        }
    }
}

@Composable
private fun getDividerColor(forStatus: OrderStatus, atIndex: Int): Color {
    when (forStatus) {
        PENDING -> {
            return colorScheme.outlineVariant
        }

        ACCEPTED -> {
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

        IN_ROUTE -> {
            if (atIndex <= 1) {
                return colorScheme.primary
            }
            return colorScheme.outlineVariant
        }

        FINISHED -> {
            return GreenPea
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelinePendingPreview() {
    OrderTimeline(status = PENDING)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineAcceptedPreview() {
    OrderTimeline(status = ACCEPTED)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineCanceledPreview() {
    OrderTimeline(status = OrderStatus.CANCELLED)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineInRoutePreview() {
    OrderTimeline(status = IN_ROUTE)
}

@Preview(showBackground = true)
@Composable
private fun OrderTimelineFinishedPreview() {
    OrderTimeline(status = FINISHED)
}