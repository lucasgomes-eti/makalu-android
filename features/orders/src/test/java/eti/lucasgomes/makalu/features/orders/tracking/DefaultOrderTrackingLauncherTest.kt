package eti.lucasgomes.makalu.features.orders.tracking

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.features.orders.orderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class DefaultOrderTrackingLauncherTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val ordersClient = mockk<OrdersClient>()
    private val tracker = mockk<OrderTracker>(relaxed = true) { every { isRunning } returns false }
    private val settings = InMemorySettings()
    private val launcher = DefaultOrderTrackingLauncher(
        app,
        ordersClient,
        TrackedOrdersStore(settings),
        tracker,
        { NOW },
        noLogger
    )

    private fun startedService() = shadowOf(app).nextStartedService

    @Test
    fun `placing an order starts the service for that order`() {
        launcher.onOrderPlaced(42)

        val intent = startedService()
        assertEquals(OrderTrackingService::class.java.name, intent.component?.className)
        assertEquals(42L, intent.getLongExtra("order_id", -1))
    }

    @Test
    fun `opening the app starts the service when an order is still in progress`() = runTest {
        coEvery { ordersClient.getOrders() } returns Resource.Success(
            listOf(
                orderSimpleResponse(
                    id = 1,
                    status = OrderStatus.IN_ROUTE,
                    updatedAt =
                        NOW - 5.minutes
                ),
                orderSimpleResponse(
                    id = 2,
                    status = OrderStatus.FINISHED,
                    updatedAt =
                        NOW - 5.minutes
                )
            )
        )

        launcher.onAppOpened()

        assertEquals(OrderTrackingService::class.java.name, startedService().component?.className)
        assertEquals(listOf(1L), settings.value.trackedOrderIds)
    }

    @Test
    fun `opening the app starts nothing when every order is over`() = runTest {
        coEvery { ordersClient.getOrders() } returns Resource.Success(
            listOf(orderSimpleResponse(id = 2, status = OrderStatus.CANCELLED, updatedAt = NOW))
        )

        launcher.onAppOpened()

        assertNull(startedService())
    }

    @Test
    fun `opening the app starts nothing for an order abandoned long ago`() = runTest {
        coEvery { ordersClient.getOrders() } returns Resource.Success(
            listOf(
                orderSimpleResponse(
                    id = 1,
                    status = OrderStatus.IN_ROUTE,
                    updatedAt =
                        NOW - 13.hours
                )
            )
        )

        launcher.onAppOpened()

        assertNull(startedService())
        assertTrue(settings.value.trackedOrderIds.isEmpty())
    }

    @Test
    fun `opening the app starts nothing when the orders cannot be fetched`() = runTest {
        coEvery { ordersClient.getOrders() } returns
            Resource.Error(MakaluError(-1, "offline", "MK-0"))

        launcher.onAppOpened()

        assertNull(startedService())
    }

    @Test
    fun `opening the app only refreshes a service that is already running`() = runTest {
        every { tracker.isRunning } returns true

        launcher.onAppOpened()

        verify { tracker.requestRefresh() }
        assertNull(startedService())
    }
}
