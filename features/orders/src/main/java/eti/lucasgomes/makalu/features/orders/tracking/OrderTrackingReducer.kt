package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import kotlin.time.Instant

/**
 * What the tracking service knows.
 *
 * @property orders the orders in progress, by id — the truth as of the last REST fetch,
 *   plus the events received since.
 * @property pendingIds orders the service was asked to follow before it could fetch
 *   anything (just placed, or remembered from before the process died).
 * @property isSynced a REST fetch has succeeded since the service started. Until then
 *   [orders] is empty and nothing may be concluded from that.
 */
internal data class TrackingState(
    val orders: Map<Long, TrackedOrder> = emptyMap(),
    val pendingIds: Set<Long> = emptySet(),
    val isSynced: Boolean = false
) {
    /** Whether the service has a reason to stay alive. */
    val shouldRun: Boolean
        get() = if (isSynced) orders.isNotEmpty() else pendingIds.isNotEmpty()

    /** Every order id worth remembering across a process death. */
    val idsToPersist: Set<Long>
        get() = if (isSynced) orders.keys else pendingIds
}

internal sealed interface TrackingEffect {
    /** The state cannot be trusted or completed from what was received: ask REST. */
    data object Refetch : TrackingEffect

    /** A followed order moved to another status that is not final. */
    data class StatusChanged(val order: TrackedOrder) : TrackingEffect

    /** A followed order reached a final status and is no longer followed. */
    data class Finished(val order: TrackedOrder) : TrackingEffect
}

internal data class Reduction(
    val state: TrackingState,
    val effects: List<TrackingEffect> = emptyList()
)

/** Pure state transitions of order tracking. No I/O, no clock of its own. */
internal object OrderTrackingReducer {

    /** The service was asked to follow [orderId]. */
    fun onTrackRequested(state: TrackingState, orderId: Long): Reduction =
        if (orderId in state.orders || orderId in state.pendingIds) {
            Reduction(state)
        } else {
            // The order's details are unknown until REST is asked, synced or not.
            Reduction(
                state.copy(pendingIds = state.pendingIds + orderId),
                listOf(TrackingEffect.Refetch)
            )
        }

    /** REST answered with the user's orders: this replaces everything known so far. */
    fun onSnapshot(
        state: TrackingState,
        snapshot: List<OrderSimpleResponse>,
        now: Instant
    ): Reduction {
        val effects = mutableListOf<TrackingEffect>()
        val byId = snapshot.associateBy { it.id }

        state.orders.values.forEach { known ->
            val fresh = byId[known.id] ?: return@forEach
            when {
                fresh.status == known.status -> Unit
                fresh.status.isFinal -> effects += TrackingEffect.Finished(fresh.toTrackedOrder())
                else -> effects += TrackingEffect.StatusChanged(fresh.toTrackedOrder())
            }
        }

        val inProgress = snapshot
            .filter { it.isInProgress(now) }
            .associate { it.id to it.toTrackedOrder() }
        return Reduction(
            TrackingState(orders = inProgress, pendingIds = emptySet(), isSynced = true),
            effects
        )
    }

    /** A message arrived on an order's channel. */
    fun onEvent(state: TrackingState, parsed: ParsedOrderEvent): Reduction {
        val event = when (parsed) {
            ParsedOrderEvent.Invalid -> return Reduction(state)
            ParsedOrderEvent.Unknown -> return Reduction(state, listOf(TrackingEffect.Refetch))
            is ParsedOrderEvent.Known -> parsed.event
        }
        val known = state.orders[event.orderId]
            ?: return Reduction(state, listOf(TrackingEffect.Refetch))
        // Not newer than what is held: a repeat, or an event overtaken by a later one.
        if (event.updatedAt <= known.updatedAt) return Reduction(state)

        val updated = known.copy(status = event.status, updatedAt = event.updatedAt)
        return when {
            event.status == known.status ->
                Reduction(state.copy(orders = state.orders + (updated.id to updated)))

            event.status.isFinal -> Reduction(
                state.copy(orders = state.orders - updated.id),
                listOf(TrackingEffect.Finished(updated))
            )

            else -> Reduction(
                state.copy(orders = state.orders + (updated.id to updated)),
                listOf(TrackingEffect.StatusChanged(updated))
            )
        }
    }

    /** The session ended (signed out): there is nothing left to follow. */
    fun onSessionEnded(): Reduction = Reduction(TrackingState(isSynced = true))
}
