package eti.lucasgomes.makalu.features.orders.tracking

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import eti.lucasgomes.makalu.shared.MkLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

internal class OrderTrackingService : Service() {
    private val tracker by inject<OrderTracker>()
    private val notifications by inject<OrderNotifications>()
    private val store by inject<TrackedOrdersStore>()
    private val logger by inject<MkLogger>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var isObserving = false

    private var foregroundId: Int? = null
    private var shownOrderIds = emptySet<Long>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            tracker.requestRefresh()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notifications.createChannels()
        if (foregroundId == null &&
            !bindForegroundIfSystemAllows(OrderNotifications.CHECKING_ID, notifications.checking())
        ) {
            stopSelf()
            return START_NOT_STICKY
        }

        val orderId = intent?.getLongExtra(EXTRA_ORDER_ID, NO_ORDER)?.takeIf { it != NO_ORDER }
        scope.launch {
            tracker.start(scope, store.read() + listOfNotNull(orderId))
            if (!isObserving) {
                isObserving = true
                launch { tracker.state.collect(::render) }
                launch { tracker.effects.collect(::alertUser) }
                getSystemService(ConnectivityManager::class.java)
                    ?.registerDefaultNetworkCallback(networkCallback)
            }
        }
        return START_STICKY
    }

    private fun render(state: TrackingState) {
        if (!state.shouldRun) {
            stopBecauseNoOrderIsInProgress()
            return
        }
        val orders = state.orders.values.sortedBy { it.id }
        if (orders.isEmpty()) return

        val bound = orders.firstOrNull { OrderNotifications.ongoingId(it.id) == foregroundId }
            ?: orders.first()
        val previous = foregroundId
        bindForegroundIfSystemAllows(
            OrderNotifications.ongoingId(bound.id),
            notifications.ongoing(bound)
        )
        if (previous == OrderNotifications.CHECKING_ID) notifications.cancelChecking()

        orders.filter { it.id != bound.id }.forEach(notifications::showOngoing)
        (shownOrderIds - state.orders.keys).forEach(notifications::cancelOngoing)
        shownOrderIds = state.orders.keys
    }

    private fun alertUser(effect: TrackingEffect) {
        when (effect) {
            is TrackingEffect.StatusChanged -> notifications.alert(effect.order)
            is TrackingEffect.Finished -> notifications.alert(effect.order)
            TrackingEffect.Refetch -> Unit
        }
    }

    private fun bindForegroundIfSystemAllows(id: Int, notification: Notification): Boolean = try {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, id, notification, type)
        foregroundId = id
        true
    } catch (exception: IllegalStateException) {
        logger.logDebug(TAG, "foreground start refused: ${exception.message}")
        false
    }

    private fun stopBecauseNoOrderIsInProgress() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        shownOrderIds.forEach(notifications::cancelOngoing)
        notifications.cancelChecking()
        stopSelf()
    }

    override fun onDestroy() {
        if (isObserving) {
            runCatching {
                getSystemService(ConnectivityManager::class.java)
                    ?.unregisterNetworkCallback(networkCallback)
            }
        }
        tracker.stop()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OrderTrackingService"
        private const val EXTRA_ORDER_ID = "order_id"
        private const val NO_ORDER = -1L

        fun intent(context: Context, orderId: Long? = null): Intent =
            Intent(context, OrderTrackingService::class.java).apply {
                orderId?.let { putExtra(EXTRA_ORDER_ID, it) }
            }
    }
}
