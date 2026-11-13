package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.features.orders.orderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.settings.Settings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderTrackerTest {

    private val ordersClient = mockk<OrdersClient>()
    private val realtime = FakeRealtimeClient()
    private val settings = InMemorySettings()
    private val tracker =
        OrderTracker(ordersClient, realtime, TrackedOrdersStore(settings), { NOW }, noLogger)

    private val updatedAt = NOW - 5.minutes

    private fun givenOrders(vararg orders: OrderSimpleResponse) {
        coEvery { ordersClient.getOrders() } returns Resource.Success(orders.toList())
    }

    private fun order(id: Long, status: OrderStatus = OrderStatus.PENDING) =
        orderSimpleResponse(id = id, status = status, updatedAt = updatedAt)

    private fun givenCredentials() {
        coEvery { ordersClient.getRealtimeCredentials(any()) } answers {
            Resource.Success(credentials(firstArg()))
        }
    }

    private fun TestScope.start(vararg ids: Long) {
        tracker.start(backgroundScope, ids.toSet())
        runCurrent()
    }

    private fun TestScope.connect() {
        realtime.isConnected.value = true
        runCurrent()
    }

    private fun fetches(count: Int) = coVerify(exactly = count) { ordersClient.getOrders() }

    @Test
    fun `with no order to look for it does not start`() = runTest {
        start()

        assertFalse(tracker.isRunning)
        assertFalse(tracker.state.value.shouldRun)
        fetches(0)
    }

    @Test
    fun `it asks REST before trusting a remembered order`() = runTest {
        givenOrders(order(1, OrderStatus.FINISHED))
        givenCredentials()

        start(1)

        assertFalse(tracker.state.value.shouldRun)
        assertTrue(settings.value.trackedOrderIds.isEmpty())
        assertTrue(realtime.connectedTo.isEmpty())
    }

    @Test
    fun `it remembers the orders it follows`() = runTest {
        givenOrders(order(1), order(2, OrderStatus.IN_ROUTE), order(3, OrderStatus.CANCELLED))
        givenCredentials()

        start(1)

        assertEquals(listOf(1L, 2L), settings.value.trackedOrderIds)
        assertTrue(tracker.state.value.shouldRun)
    }

    @Test
    fun `it follows every order in progress over one connection`() = runTest {
        givenOrders(order(1), order(2, OrderStatus.IN_ROUTE))
        givenCredentials()
        start(1)

        connect()

        assertEquals(setOf("wss://broker.test/"), realtime.connectedTo.toSet())
        assertEquals(
            setOf("key-1/makalu/order/1/", "key-2/makalu/order/2/"),
            realtime.subscriptions.toSet()
        )
    }

    @Test
    fun `it refetches over REST on every connect and reconnect`() = runTest {
        givenOrders(order(1))
        givenCredentials()
        start(1)
        fetches(1)

        connect()
        fetches(2)

        realtime.isConnected.value = false
        runCurrent()
        connect()
        fetches(3)
        // A new connection has no subscriptions: they are made again.
        assertEquals(2, realtime.subscriptions.count { it == "key-1/makalu/order/1/" })
    }

    @Test
    fun `a status event updates the order and is announced`() = runTest {
        givenOrders(order(1))
        givenCredentials()
        start(1)
        connect()
        val effects = mutableListOf<TrackingEffect>()
        backgroundScope.launch { tracker.effects.collect(effects::add) }
        runCurrent()

        realtime.deliver(1, "ACCEPTED", "2026-01-10T12:34:00Z")
        runCurrent()

        assertEquals(OrderStatus.ACCEPTED, tracker.state.value.orders[1]?.status)
        assertEquals(OrderStatus.ACCEPTED, tracker.statuses.value[1])
        assertTrue(effects.single() is TrackingEffect.StatusChanged)
    }

    @Test
    fun `a final status stops the tracking and stays readable by the screens`() = runTest {
        givenOrders(order(1, OrderStatus.IN_ROUTE))
        givenCredentials()
        start(1)
        connect()

        realtime.deliver(1, "FINISHED", "2026-01-10T12:34:00Z")
        runCurrent()

        assertFalse(tracker.state.value.shouldRun)
        assertEquals(OrderStatus.FINISHED, tracker.statuses.value[1])
    }

    @Test
    fun `a stale event changes nothing and fetches nothing`() = runTest {
        givenOrders(order(1, OrderStatus.IN_ROUTE))
        givenCredentials()
        start(1)
        connect()
        fetches(2)

        realtime.deliver(1, "ACCEPTED", "2026-01-10T12:00:00Z")
        runCurrent()

        assertEquals(OrderStatus.IN_ROUTE, tracker.state.value.orders[1]?.status)
        fetches(2)
    }

    @Test
    fun `an event it cannot apply makes it refetch`() = runTest {
        givenOrders(order(1))
        givenCredentials()
        start(1)
        connect()
        fetches(2)

        realtime.deliver(1, "PENDING", "2026-01-10T12:34:00Z", type = "order.refunded")
        runCurrent()
        fetches(3)

        realtime.deliver(99, "ACCEPTED", "2026-01-10T12:34:00Z")
        runCurrent()
        fetches(4)
    }

    @Test
    fun `an order placed while it runs is followed too`() = runTest {
        givenOrders(order(1))
        givenCredentials()
        start(1)
        connect()
        givenOrders(order(1), order(2))

        tracker.start(backgroundScope, setOf(2))
        runCurrent()

        assertEquals(setOf(1L, 2L), tracker.state.value.orders.keys)
        assertTrue("key-2/makalu/order/2/" in realtime.subscriptions)
    }

    @Test
    fun `it asks for new credentials when the broker refuses the key`() = runTest {
        givenOrders(order(1))
        coEvery { ordersClient.getRealtimeCredentials(1) } returnsMany listOf(
            Resource.Success(credentials(1, key = "stale")),
            Resource.Success(credentials(1, key = "fresh"))
        )
        realtime.refusedKeys += "stale"
        start(1)
        connect()
        assertTrue(realtime.subscriptions.isEmpty())

        // Nothing is live, so the poll fetches again and subscribes with a new key.
        advanceTimeBy(OrderTracker.POLL_INTERVAL)
        runCurrent()

        assertEquals(listOf("fresh/makalu/order/1/"), realtime.subscriptions)
    }

    @Test
    fun `it renews credentials that are about to expire before reusing them`() = runTest {
        givenOrders(order(1))
        coEvery { ordersClient.getRealtimeCredentials(1) } returnsMany listOf(
            Resource.Success(credentials(1, key = "expiring", expiresAt = NOW + 1.minutes)),
            Resource.Success(credentials(1, key = "renewed"))
        )
        start(1)
        connect()

        realtime.isConnected.value = false
        runCurrent()
        connect()

        assertEquals("renewed/makalu/order/1/", realtime.subscriptions.last())
    }

    @Test
    fun `it keeps polling REST while realtime is switched off`() = runTest {
        givenOrders(order(1))
        coEvery { ordersClient.getRealtimeCredentials(any()) } returns
            Resource.Error(MakaluError(503, "Realtime updates are unavailable", "MK-801"))
        start(1)
        fetches(1)

        givenOrders(order(1, OrderStatus.ACCEPTED))
        advanceTimeBy(OrderTracker.POLL_INTERVAL)
        runCurrent()

        fetches(2)
        assertEquals(OrderStatus.ACCEPTED, tracker.state.value.orders[1]?.status)
        assertTrue(realtime.connectedTo.isEmpty())
    }

    @Test
    fun `it stops polling once every order has a live subscription`() = runTest {
        givenOrders(order(1))
        givenCredentials()
        start(1)
        connect()
        fetches(2)

        advanceTimeBy(OrderTracker.POLL_INTERVAL * 3)
        runCurrent()

        fetches(2)
    }

    @Test
    fun `it keeps the remembered order and retries when REST is unreachable`() = runTest {
        coEvery { ordersClient.getOrders() } returns
            Resource.Error(MakaluError(-1, "offline", "MK-0"))
        start(1)

        assertTrue(tracker.state.value.shouldRun)
        assertEquals(listOf(1L), settings.value.trackedOrderIds)

        givenOrders(order(1))
        givenCredentials()
        advanceTimeBy(OrderTracker.RETRY_AFTER_FAILURE)
        runCurrent()

        assertTrue(tracker.state.value.isSynced)
        assertEquals(setOf(1L), tracker.state.value.orders.keys)
    }

    @Test
    fun `it has nothing left to follow once the session has ended`() = runTest {
        coEvery { ordersClient.getOrders() } returns
            Resource.Error(MakaluError(401, "Unauthorized", "MK-101"))

        start(1)

        assertFalse(tracker.state.value.shouldRun)
        assertTrue(settings.value.trackedOrderIds.isEmpty())
    }

    @Test
    fun `it unsubscribes from an order that is over`() = runTest {
        givenOrders(order(1), order(2))
        givenCredentials()
        start(1)
        connect()

        givenOrders(order(1, OrderStatus.CANCELLED), order(2))
        tracker.requestRefresh()
        runCurrent()

        assertEquals(listOf("key-1/makalu/order/1/"), realtime.unsubscriptions)
        assertEquals(setOf(2L), tracker.state.value.orders.keys)
    }

    @Test
    fun `stopping closes the connection`() = runTest {
        givenOrders(order(1))
        givenCredentials()
        start(1)
        connect()

        tracker.stop()

        assertFalse(tracker.isRunning)
        assertEquals(1, realtime.disconnects)
    }

    @Test
    fun `a restart after process death starts from the remembered orders`() = runTest {
        val restored = InMemorySettings(Settings(trackedOrderIds = listOf(4)))
        val store = TrackedOrdersStore(restored)
        val revived = OrderTracker(ordersClient, realtime, store, { NOW }, noLogger)
        givenOrders(order(4, OrderStatus.IN_ROUTE))
        givenCredentials()

        revived.start(backgroundScope, store.read())
        runCurrent()

        assertEquals(setOf(4L), revived.state.value.orders.keys)
        fetches(1)
    }
}
