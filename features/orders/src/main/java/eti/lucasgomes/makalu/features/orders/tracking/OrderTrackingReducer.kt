package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import kotlin.time.Instant

internal data class TrackingState(
    val orders: Map<Long, TrackedOrder> = emptyMap(),
    val pendingIds: Set<Long> = emptySet(),
    val isSynced: Boolean = false
) {
    val shouldRun: Boolean
        get() = if (isSynced) orders.isNotEmpty() else pendingIds.isNotEmpty()

    val idsToPersist: Set<Long>
        get() = if (isSynced) orders.keys else pendingIds
}

internal sealed interface TrackingEffect {
    data object Refetch : TrackingEffect

    data class StatusChanged(val order: TrackedOrder) : TrackingEffect

    data class Finished(val order: TrackedOrder) : TrackingEffect
}

internal data class Reduction(
    val state: TrackingState,
    val effects: List<TrackingEffect> = emptyList()
)

internal object OrderTrackingReducer {
    fun onTrackRequested(state: TrackingState, orderId: Long): Reduction =
        if (orderId in state.orders || orderId in state.pendingIds) {
            Reduction(state)
        } else {
            Reduction(
                state.copy(pendingIds = state.pendingIds + orderId),
                listOf(TrackingEffect.Refetch)
            )
        }

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

    fun onEvent(state: TrackingState, parsed: ParsedOrderEvent): Reduction {
        val event = when (parsed) {
            ParsedOrderEvent.Invalid -> return Reduction(state)
            ParsedOrderEvent.Unknown -> return Reduction(state, listOf(TrackingEffect.Refetch))
            is ParsedOrderEvent.Known -> parsed.event
        }
        val known = state.orders[event.orderId]
            ?: return Reduction(state, listOf(TrackingEffect.Refetch))
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

    fun onSessionEnded(): Reduction = Reduction(TrackingState(isSynced = true))
}
