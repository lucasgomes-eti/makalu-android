package eti.lucasgomes.makalu.features.orders.tracking

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import eti.lucasgomes.makalu.features.orders.R
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.tracking.OrderTrackingLauncher

/**
 * Builds and posts the notifications of order tracking.
 *
 * - One ongoing, silent notification per order in progress, on a low-importance channel,
 *   with the progress through the order's steps. Shown as a Live Update where the device
 *   supports it (Android 16+), as a plain progress bar elsewhere.
 * - One audible alert per status change, on a separate high-importance channel. For a
 *   final status the alert stays until dismissed and takes the ongoing one's place.
 *
 * Posting is a no-op while the user has notifications turned off; the service and the
 * in-app screens keep working without them.
 */
internal class OrderNotifications(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun createChannels() {
        manager.createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat
                    .Builder(CHANNEL_ONGOING, NotificationManagerCompat.IMPORTANCE_LOW)
                    .setName(context.getString(R.string.tracking_channel_ongoing_name))
                    .setDescription(
                        context.getString(R.string.tracking_channel_ongoing_description)
                    )
                    .setShowBadge(false)
                    .build(),
                NotificationChannelCompat
                    .Builder(CHANNEL_UPDATES, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName(context.getString(R.string.tracking_channel_updates_name))
                    .setDescription(
                        context.getString(R.string.tracking_channel_updates_description)
                    )
                    .build()
            )
        )
    }

    /** Shown while the service does not yet know what it is following. */
    fun checking(): Notification = NotificationCompat.Builder(context, CHANNEL_ONGOING)
        .setSmallIcon(R.drawable.schedule)
        .setContentTitle(context.getString(R.string.tracking_checking_title))
        .setContentText(context.getString(R.string.tracking_checking_text))
        .setOngoing(true)
        .setSilent(true)
        .setProgress(0, 0, true)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .build()

    fun ongoing(order: TrackedOrder): Notification {
        val step = order.status.step
        val builder = NotificationCompat.Builder(context, CHANNEL_ONGOING)
            .setSmallIcon(order.status.icon)
            .setContentTitle(order.storeName)
            .setContentText(
                context.getString(
                    R.string.tracking_ongoing_text,
                    order.orderNumber,
                    context.getString(order.status.message)
                )
            )
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(openOrder(order.id))
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            // Ignored where Live Updates do not exist or the user turned them off.
            .setRequestPromotedOngoing(true)
            .setShortCriticalText(context.getString(order.status.label))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            builder.setStyle(
                NotificationCompat.ProgressStyle()
                    .setProgressSegments(
                        List(LAST_STEP) { NotificationCompat.ProgressStyle.Segment(1) }
                    )
                    .setProgress(step)
            )
        } else {
            builder.setProgress(LAST_STEP, step, false)
        }
        return builder.build()
    }

    @SuppressLint("MissingPermission") // Guarded by areNotificationsEnabled().
    fun showOngoing(order: TrackedOrder) {
        if (manager.areNotificationsEnabled()) manager.notify(ongoingId(order.id), ongoing(order))
    }

    fun cancelOngoing(orderId: Long) = manager.cancel(ongoingId(orderId))

    fun cancelChecking() = manager.cancel(CHECKING_ID)

    /** The audible alert for a status change. A final status stays until dismissed. */
    @SuppressLint("MissingPermission") // Guarded by areNotificationsEnabled().
    fun alert(order: TrackedOrder) {
        if (!manager.areNotificationsEnabled()) return
        val builder = NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(order.status.icon)
            .setContentTitle(
                context.getString(
                    R.string.tracking_update_title,
                    order.storeName,
                    context.getString(order.status.message)
                )
            )
            .setContentText(context.getString(R.string.tracking_update_text, order.orderNumber))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openOrder(order.id))
        // While the order is in progress the ongoing notification carries its status;
        // the alert is only there to make a sound and then gets out of the way.
        if (!order.status.isFinal) builder.setTimeoutAfter(ALERT_TIMEOUT_MILLIS)
        manager.notify(ALERT_TAG, ongoingId(order.id), builder.build())
    }

    private fun openOrder(orderId: Long): PendingIntent {
        val intent = (
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
            )
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(OrderTrackingLauncher.EXTRA_ORDER_ID, orderId)
        return PendingIntent.getActivity(
            context,
            ongoingId(orderId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ONGOING = "orders_in_progress"
        const val CHANNEL_UPDATES = "order_updates"
        const val CHECKING_ID = 1
        private const val ALERT_TAG = "order_update"
        private const val ALERT_TIMEOUT_MILLIS = 20_000L
        private const val ID_RANGE = 1_000_000_000L
        private const val FIRST_ORDER_ID = 1_000
        const val LAST_STEP = 3

        /** Notification id of an order's ongoing notification; never [CHECKING_ID]. */
        fun ongoingId(orderId: Long): Int = FIRST_ORDER_ID + (orderId % ID_RANGE).toInt()
    }
}

/** How far along the order is, out of [OrderNotifications.LAST_STEP]. */
internal val OrderStatus.step: Int
    get() = when (this) {
        OrderStatus.PENDING, OrderStatus.CANCELLED -> 0
        OrderStatus.ACCEPTED -> 1
        OrderStatus.IN_ROUTE -> 2
        OrderStatus.FINISHED -> OrderNotifications.LAST_STEP
    }

private val OrderStatus.icon: Int
    get() = when (this) {
        OrderStatus.PENDING -> R.drawable.schedule
        OrderStatus.ACCEPTED -> R.drawable.restaurant
        OrderStatus.IN_ROUTE -> R.drawable.two_wheeler
        OrderStatus.FINISHED -> R.drawable.check_circle
        OrderStatus.CANCELLED -> R.drawable.schedule
    }

private val OrderStatus.label: Int
    get() = when (this) {
        OrderStatus.PENDING -> R.string.order_status_pending
        OrderStatus.ACCEPTED -> R.string.order_status_accepted
        OrderStatus.CANCELLED -> R.string.order_status_cancelled
        OrderStatus.IN_ROUTE -> R.string.order_status_in_route
        OrderStatus.FINISHED -> R.string.order_status_finished
    }

private val OrderStatus.message: Int
    get() = when (this) {
        OrderStatus.PENDING -> R.string.tracking_message_pending
        OrderStatus.ACCEPTED -> R.string.tracking_message_accepted
        OrderStatus.IN_ROUTE -> R.string.tracking_message_in_route
        OrderStatus.FINISHED -> R.string.tracking_message_finished
        OrderStatus.CANCELLED -> R.string.tracking_message_cancelled
    }
