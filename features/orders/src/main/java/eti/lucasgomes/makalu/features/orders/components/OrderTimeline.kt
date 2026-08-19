package eti.lucasgomes.makalu.features.orders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.model.OrderStatus.ACCEPTED
import eti.lucasgomes.makalu.shared.model.OrderStatus.CANCELLED
import eti.lucasgomes.makalu.shared.model.OrderStatus.FINISHED
import eti.lucasgomes.makalu.shared.model.OrderStatus.IN_ROUTE
import eti.lucasgomes.makalu.shared.model.OrderStatus.PENDING

private val timelineSteps = listOf(PENDING, ACCEPTED, IN_ROUTE, FINISHED)

private val StepSize = 28.dp
private val StepPadding = 4.dp
private val ConnectorThickness = 2.dp

@Composable
internal fun OrderTimeline(
    modifier: Modifier = Modifier,
    status: OrderStatus
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        timelineSteps.forEachIndexed { index, step ->
            if (index > 0) {
                StepConnector(color = connectorColor(status, atIndex = index - 1))
            }
            StepIcon(step = step, currentStatus = status)
        }
    }
}

@Composable
private fun StepIcon(step: OrderStatus, currentStatus: OrderStatus) {
    val state = step.stateWithin(currentStatus)
    // A canceled order takes over the step it stopped at, so that slot is painted red.
    val palette = if (state == StepState.CURRENT) currentStatus.palette() else step.palette()
    val backgroundColor = when (state) {
        StepState.UPCOMING -> colorScheme.surfaceDim
        StepState.CURRENT -> palette.strong
        StepState.COMPLETED -> palette.soft
    }
    val tint = when (state) {
        StepState.UPCOMING -> colorScheme.inverseOnSurface
        StepState.CURRENT -> palette.onStrong
        StepState.COMPLETED -> palette.onSoft
    }
    val iconRes = if (state == StepState.COMPLETED) R.drawable.check else palette.icon

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(StepSize)
            .background(backgroundColor, shape = CircleShape)
            .padding(StepPadding)
    )
}

@Composable
private fun RowScope.StepConnector(color: Color) {
    HorizontalDivider(
        modifier = Modifier.weight(1f),
        thickness = ConnectorThickness,
        color = color
    )
}

private enum class StepState { UPCOMING, CURRENT, COMPLETED }

/**
 * Where a status sits on the timeline. A canceled order stops on the [ACCEPTED] step, so both
 * share an index.
 */
private val OrderStatus.timelineIndex: Int
    get() = when (this) {
        PENDING -> 0
        ACCEPTED, CANCELLED -> 1
        IN_ROUTE -> 2
        FINISHED -> 3
    }

private fun OrderStatus.stateWithin(currentStatus: OrderStatus): StepState {
    val currentIndex = currentStatus.timelineIndex
    return when {
        timelineIndex < currentIndex -> StepState.COMPLETED
        timelineIndex == currentIndex -> StepState.CURRENT
        else -> StepState.UPCOMING
    }
}

@Composable
private fun connectorColor(currentStatus: OrderStatus, atIndex: Int): Color =
    if (atIndex < currentStatus.timelineIndex) currentStatus.palette().strong
    else colorScheme.outlineVariant

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
    OrderTimeline(status = CANCELLED)
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
