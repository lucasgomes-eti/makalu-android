package eti.lucasgomes.makalu.features.orders.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import eti.lucasgomes.makalu.components.DairyCream
import eti.lucasgomes.makalu.components.GreenPea
import eti.lucasgomes.makalu.components.HawkesBlue
import eti.lucasgomes.makalu.components.Himalaya
import eti.lucasgomes.makalu.components.ScienceBlue
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.shared.model.OrderStatus

/**
 * How a single [OrderStatus] is painted, shared by [OrderStatusTag] and [OrderTimeline].
 *
 * Every status owns a strong/soft color pair; the two components use the same pair with the
 * roles swapped. [OrderStatus.CANCELLED] and [OrderStatus.FINISHED] are terminal, so they never
 * render as a completed timeline step and their soft slot stays equal to the strong one.
 */
internal data class OrderStatusPalette(
    val strong: Color,
    val onStrong: Color,
    val soft: Color,
    val onSoft: Color,
    @param:DrawableRes val icon: Int
)

@Composable
@ReadOnlyComposable
internal fun OrderStatus.palette(): OrderStatusPalette = when (this) {
    OrderStatus.PENDING -> OrderStatusPalette(
        strong = Himalaya,
        onStrong = Color.White,
        soft = DairyCream,
        onSoft = Himalaya,
        icon = R.drawable.schedule
    )

    OrderStatus.ACCEPTED -> OrderStatusPalette(
        strong = ScienceBlue,
        onStrong = Color.White,
        soft = HawkesBlue,
        onSoft = ScienceBlue,
        icon = R.drawable.restaurant
    )

    OrderStatus.CANCELLED -> OrderStatusPalette(
        strong = colorScheme.error,
        onStrong = Color.White,
        soft = colorScheme.error,
        onSoft = Color.White,
        icon = eti.lucasgomes.makalu.components.R.drawable.close
    )

    OrderStatus.IN_ROUTE -> OrderStatusPalette(
        strong = colorScheme.primary,
        onStrong = Color.White,
        soft = colorScheme.primaryContainer,
        onSoft = colorScheme.primary,
        icon = R.drawable.two_wheeler
    )

    OrderStatus.FINISHED -> OrderStatusPalette(
        strong = GreenPea,
        onStrong = Color.White,
        soft = GreenPea,
        onSoft = Color.White,
        icon = R.drawable.check_circle
    )
}
