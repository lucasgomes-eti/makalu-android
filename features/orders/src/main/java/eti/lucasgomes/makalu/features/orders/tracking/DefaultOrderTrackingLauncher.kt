package eti.lucasgomes.makalu.features.orders.tracking

import android.content.Context
import androidx.core.content.ContextCompat
import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.shared.MkLogger
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.tracking.OrderTrackingLauncher
import kotlin.time.Instant

internal class DefaultOrderTrackingLauncher(
    private val context: Context,
    private val ordersClient: OrdersClient,
    private val store: TrackedOrdersStore,
    private val tracker: OrderTracker,
    private val clock: () -> Instant,
    private val logger: MkLogger
) : OrderTrackingLauncher {

    override fun onOrderPlaced(orderId: Long) {
        startService(orderId)
    }

    override suspend fun onAppOpened() {
        if (tracker.isRunning) {
            // The app was away: catch up on anything missed meanwhile.
            tracker.requestRefresh()
            return
        }
        val orders = (ordersClient.getOrders() as? Resource.Success)?.data ?: return
        val inProgress = orders.filter { it.isInProgress(clock()) }.map { it.id }.toSet()
        store.write(inProgress)
        if (inProgress.isNotEmpty()) startService(orderId = null)
    }

    private fun startService(orderId: Long?) {
        try {
            ContextCompat.startForegroundService(
                context,
                OrderTrackingService.intent(context, orderId)
            )
        } catch (exception: IllegalStateException) {
            // Not allowed from the background. The order is still there; tracking
            // starts the next time the app is opened.
            logger.logDebug("OrderTracking", "service start refused: ${exception.message}")
        }
    }
}
