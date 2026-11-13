package eti.lucasgomes.makalu.features.orders.tracking

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.tracking.OrderTrackingLauncher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class OrderNotificationsTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val manager = app.getSystemService(NotificationManager::class.java)
    private val notifications = OrderNotifications(app)

    private fun order(status: OrderStatus, id: Long = 42) =
        TrackedOrder(id, "0001", "Sushi Naka", status, NOW)

    private val Notification.isOngoing get() = flags and Notification.FLAG_ONGOING_EVENT != 0
    private val Notification.title get() = extras.getCharSequence(
        Notification.EXTRA_TITLE
    ).toString()
    private val Notification.text get() = extras.getCharSequence(Notification.EXTRA_TEXT).toString()

    @Before
    fun setUp() = notifications.createChannels()

    @Test
    fun `the ongoing channel is silent and the updates channel is not`() {
        assertEquals(
            NotificationManager.IMPORTANCE_LOW,
            manager.getNotificationChannel(OrderNotifications.CHANNEL_ONGOING).importance
        )
        assertEquals(
            NotificationManager.IMPORTANCE_HIGH,
            manager.getNotificationChannel(OrderNotifications.CHANNEL_UPDATES).importance
        )
    }

    @Test
    fun `an order in progress gets an ongoing notification with the store and its status`() {
        val notification = notifications.ongoing(order(OrderStatus.IN_ROUTE))

        assertTrue(notification.isOngoing)
        assertEquals(OrderNotifications.CHANNEL_ONGOING, notification.channelId)
        assertEquals("Sushi Naka", notification.title)
        assertEquals("Order Nº 0001 · Your order is on its way", notification.text)
    }

    @Test
    fun `every status can be shown`() {
        OrderStatus.entries.forEach { status -> notifications.ongoing(order(status)) }
    }

    @Test
    fun `progress follows the steps of an order`() {
        assertEquals(
            listOf(0, 1, 2, 3),
            listOf(
                OrderStatus.PENDING,
                OrderStatus.ACCEPTED,
                OrderStatus.IN_ROUTE,
                OrderStatus.FINISHED
            ).map { it.step }
        )
    }

    @Test
    fun `tapping the notification opens the app on that order`() {
        val notification = notifications.ongoing(order(OrderStatus.ACCEPTED, id = 77))

        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(77L, intent.getLongExtra(OrderTrackingLauncher.EXTRA_ORDER_ID, -1))
    }

    @Test
    fun `each order has its own notification, never the placeholder's`() {
        assertNotEquals(OrderNotifications.ongoingId(1), OrderNotifications.ongoingId(2))
        assertNotEquals(OrderNotifications.CHECKING_ID, OrderNotifications.ongoingId(0))
    }

    @Test
    fun `a status change alerts on the updates channel and then goes away by itself`() {
        notifications.alert(order(OrderStatus.ACCEPTED))

        val alert = shadowOf(manager).allNotifications.single()
        assertEquals(OrderNotifications.CHANNEL_UPDATES, alert.channelId)
        assertEquals("Sushi Naka: The store is preparing your order", alert.title)
        assertFalse(alert.isOngoing)
        assertTrue(alert.timeoutAfter > 0)
    }

    @Test
    fun `a final status leaves a normal notification the user dismisses`() {
        notifications.alert(order(OrderStatus.FINISHED))

        val alert = shadowOf(manager).allNotifications.single()
        assertEquals("Sushi Naka: Your order was delivered", alert.title)
        assertFalse(alert.isOngoing)
        assertEquals(0L, alert.timeoutAfter)
        assertTrue(alert.flags and Notification.FLAG_AUTO_CANCEL != 0)
    }

    @Test
    fun `nothing is posted while notifications are turned off`() {
        shadowOf(manager).setNotificationsEnabled(false)

        notifications.alert(order(OrderStatus.ACCEPTED))
        notifications.showOngoing(order(OrderStatus.ACCEPTED))

        assertTrue(shadowOf(manager).allNotifications.isEmpty())
    }
}
