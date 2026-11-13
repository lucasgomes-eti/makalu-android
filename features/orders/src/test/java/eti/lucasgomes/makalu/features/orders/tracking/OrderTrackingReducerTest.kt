package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.features.orders.orderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderTrackingReducerTest {

    private val t0 = Instant.parse("2026-01-10T12:00:00Z")
    private val now = t0 + 5.minutes

    private fun tracked(id: Long = 1, status: OrderStatus = OrderStatus.PENDING, at: Instant = t0) =
        TrackedOrder(id, "0001", "Sushi Naka", status, at)

    private fun synced(vararg orders: TrackedOrder) =
        TrackingState(orders = orders.associateBy { it.id }, isSynced = true)

    private fun event(
        id: Long = 1,
        status: OrderStatus = OrderStatus.ACCEPTED,
        at: Instant = t0 + 1.minutes
    ) = ParsedOrderEvent.Known(OrderEvent(id, status, at))

    // region start and stop decisions

    @Test
    fun `a service with nothing to follow should not run`() {
        assertFalse(TrackingState().shouldRun)
        assertFalse(TrackingState(isSynced = true).shouldRun)
    }

    @Test
    fun `an order requested before the first fetch keeps the service running`() {
        val reduction = OrderTrackingReducer.onTrackRequested(TrackingState(), 7)

        assertTrue(reduction.state.shouldRun)
        assertEquals(setOf(7L), reduction.state.pendingIds)
        assertEquals(setOf(7L), reduction.state.idsToPersist)
        assertEquals(listOf(TrackingEffect.Refetch), reduction.effects)
    }

    @Test
    fun `requesting an order already followed changes nothing`() {
        val state = synced(tracked(1))

        assertEquals(Reduction(state), OrderTrackingReducer.onTrackRequested(state, 1))
    }

    @Test
    fun `an order requested after the first fetch is fetched, not assumed`() {
        val reduction = OrderTrackingReducer.onTrackRequested(synced(tracked(1)), 2)

        assertEquals(listOf(TrackingEffect.Refetch), reduction.effects)
        assertEquals(setOf(1L), reduction.state.orders.keys)
    }

    @Test
    fun `the service runs while the fetched orders include one in progress`() {
        val reduction = OrderTrackingReducer.onSnapshot(
            TrackingState(pendingIds = setOf(1)),
            listOf(
                orderSimpleResponse(id = 1, status = OrderStatus.ACCEPTED, updatedAt = t0),
                orderSimpleResponse(id = 2, status = OrderStatus.FINISHED, updatedAt = t0)
            ),
            now
        )

        assertTrue(reduction.state.shouldRun)
        assertEquals(setOf(1L), reduction.state.orders.keys)
        assertEquals(setOf(1L), reduction.state.idsToPersist)
        assertTrue(reduction.state.pendingIds.isEmpty())
    }

    @Test
    fun `the service stops when the fetch shows no order in progress`() {
        val reduction = OrderTrackingReducer.onSnapshot(
            TrackingState(pendingIds = setOf(1)),
            listOf(orderSimpleResponse(id = 1, status = OrderStatus.CANCELLED, updatedAt = t0)),
            now
        )

        assertFalse(reduction.state.shouldRun)
        assertTrue(reduction.state.idsToPersist.isEmpty())
    }

    @Test
    fun `a remembered order is not trusted until the fetch confirms it`() {
        val restored = TrackingState(pendingIds = setOf(1, 2))

        val reduction = OrderTrackingReducer.onSnapshot(
            restored,
            listOf(orderSimpleResponse(id = 2, status = OrderStatus.IN_ROUTE, updatedAt = t0)),
            now
        )

        assertEquals(setOf(2L), reduction.state.orders.keys)
    }

    @Test
    fun `the service follows every order in progress, not only the one it was started for`() {
        val reduction = OrderTrackingReducer.onSnapshot(
            TrackingState(pendingIds = setOf(1)),
            listOf(
                orderSimpleResponse(id = 1, updatedAt = t0),
                orderSimpleResponse(id = 3, status = OrderStatus.IN_ROUTE, updatedAt = t0)
            ),
            now
        )

        assertEquals(setOf(1L, 3L), reduction.state.orders.keys)
    }

    @Test
    fun `an order that has not changed for twelve hours is no longer followed`() {
        val reduction = OrderTrackingReducer.onSnapshot(
            TrackingState(),
            listOf(
                orderSimpleResponse(id = 1, status = OrderStatus.IN_ROUTE, updatedAt = t0),
                orderSimpleResponse(id = 2, status = OrderStatus.IN_ROUTE, updatedAt = t0 + 1.hours)
            ),
            t0 + 12.hours
        )

        assertEquals(setOf(2L), reduction.state.orders.keys)
    }

    @Test
    fun `the service stops when the last followed order reaches a final status`() {
        val reduction = OrderTrackingReducer.onEvent(
            synced(tracked(1, OrderStatus.IN_ROUTE)),
            event(status = OrderStatus.FINISHED)
        )

        assertFalse(reduction.state.shouldRun)
    }

    @Test
    fun `the service keeps running for the other orders when one finishes`() {
        val reduction = OrderTrackingReducer.onEvent(
            synced(tracked(1, OrderStatus.IN_ROUTE), tracked(2)),
            event(id = 1, status = OrderStatus.FINISHED)
        )

        assertTrue(reduction.state.shouldRun)
        assertEquals(setOf(2L), reduction.state.orders.keys)
    }

    @Test
    fun `the end of the session leaves nothing to follow`() {
        assertFalse(OrderTrackingReducer.onSessionEnded().state.shouldRun)
    }

    // endregion

    // region events

    @Test
    fun `a newer status is applied and announced`() {
        val reduction = OrderTrackingReducer.onEvent(synced(tracked(1)), event())

        val expected = tracked(1, OrderStatus.ACCEPTED, t0 + 1.minutes)
        assertEquals(expected, reduction.state.orders[1])
        assertEquals(listOf(TrackingEffect.StatusChanged(expected)), reduction.effects)
    }

    @Test
    fun `events arriving in order are applied one after the other`() {
        var state = synced(tracked(1))

        state = OrderTrackingReducer.onEvent(state, event(at = t0 + 1.minutes)).state
        state = OrderTrackingReducer.onEvent(
            state,
            event(status = OrderStatus.IN_ROUTE, at = t0 + 2.minutes)
        ).state

        assertEquals(OrderStatus.IN_ROUTE, state.orders[1]?.status)
    }

    @Test
    fun `an event that arrives after a newer one is dropped`() {
        val state = synced(tracked(1, OrderStatus.IN_ROUTE, t0 + 2.minutes))

        val reduction = OrderTrackingReducer.onEvent(state, event(at = t0 + 1.minutes))

        assertEquals(Reduction(state), reduction)
    }

    @Test
    fun `an event as old as the state held is dropped, so a repeat announces nothing`() {
        val state = synced(tracked(1, OrderStatus.ACCEPTED, t0 + 1.minutes))

        val reduction = OrderTrackingReducer.onEvent(state, event(at = t0 + 1.minutes))

        assertEquals(Reduction(state), reduction)
    }

    @Test
    fun `a newer event with the same status only moves the timestamp`() {
        val reduction = OrderTrackingReducer.onEvent(
            synced(tracked(1, OrderStatus.ACCEPTED, t0)),
            event(at = t0 + 1.minutes)
        )

        assertEquals(t0 + 1.minutes, reduction.state.orders[1]?.updatedAt)
        assertTrue(reduction.effects.isEmpty())
    }

    @Test
    fun `an event for an order that is not followed asks for a refetch`() {
        val state = synced(tracked(1))

        val reduction = OrderTrackingReducer.onEvent(state, event(id = 99))

        assertEquals(Reduction(state, listOf(TrackingEffect.Refetch)), reduction)
    }

    @Test
    fun `an event of an unknown kind asks for a refetch and changes nothing`() {
        val state = synced(tracked(1))

        val reduction = OrderTrackingReducer.onEvent(state, ParsedOrderEvent.Unknown)

        assertEquals(Reduction(state, listOf(TrackingEffect.Refetch)), reduction)
    }

    @Test
    fun `a message that is not an order event is ignored`() {
        val state = synced(tracked(1))

        assertEquals(
            Reduction(state),
            OrderTrackingReducer.onEvent(state, ParsedOrderEvent.Invalid)
        )
    }

    // endregion

    // region final statuses

    @Test
    fun `finishing an order stops following it and announces the end`() {
        val reduction = OrderTrackingReducer.onEvent(
            synced(tracked(1, OrderStatus.IN_ROUTE)),
            event(status = OrderStatus.FINISHED)
        )

        assertTrue(reduction.state.orders.isEmpty())
        assertEquals(
            listOf(TrackingEffect.Finished(tracked(1, OrderStatus.FINISHED, t0 + 1.minutes))),
            reduction.effects
        )
    }

    @Test
    fun `cancelling an order stops following it and announces the end`() {
        val reduction = OrderTrackingReducer.onEvent(
            synced(tracked(1)),
            event(status = OrderStatus.CANCELLED)
        )

        assertTrue(reduction.state.orders.isEmpty())
        assertTrue(reduction.effects.single() is TrackingEffect.Finished)
    }

    @Test
    fun `an event after the end asks for a refetch instead of reviving the order`() {
        val finished = OrderTrackingReducer.onEvent(
            synced(tracked(1, OrderStatus.IN_ROUTE)),
            event(status = OrderStatus.FINISHED)
        ).state

        val reduction = OrderTrackingReducer.onEvent(
            finished,
            event(status = OrderStatus.IN_ROUTE, at = t0 + 3.minutes)
        )

        assertTrue(reduction.state.orders.isEmpty())
        assertEquals(listOf(TrackingEffect.Refetch), reduction.effects)
    }

    @Test
    fun `a fetch announces an order that finished while events were missed`() {
        val reduction = OrderTrackingReducer.onSnapshot(
            synced(tracked(1, OrderStatus.IN_ROUTE)),
            listOf(
                orderSimpleResponse(
                    id = 1,
                    status = OrderStatus.FINISHED,
                    updatedAt = t0 + 2.minutes
                )
            ),
            now
        )

        assertTrue(reduction.state.orders.isEmpty())
        assertEquals(
            OrderStatus.FINISHED,
            (reduction.effects.single() as TrackingEffect.Finished).order.status
        )
    }

    @Test
    fun `a fetch announces a status that changed while events were missed`() {
        val reduction = OrderTrackingReducer.onSnapshot(
            synced(tracked(1)),
            listOf(
                orderSimpleResponse(
                    id = 1,
                    status = OrderStatus.IN_ROUTE,
                    updatedAt = t0 + 2.minutes
                )
            ),
            now
        )

        assertEquals(OrderStatus.IN_ROUTE, reduction.state.orders[1]?.status)
        assertTrue(reduction.effects.single() is TrackingEffect.StatusChanged)
    }

    @Test
    fun `a fetch that confirms what is known announces nothing`() {
        val reduction = OrderTrackingReducer.onSnapshot(
            synced(tracked(1, OrderStatus.ACCEPTED)),
            listOf(orderSimpleResponse(id = 1, status = OrderStatus.ACCEPTED, updatedAt = t0)),
            now
        )

        assertTrue(reduction.effects.isEmpty())
    }

    // endregion
}
