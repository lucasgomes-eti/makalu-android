package eti.lucasgomes.makalu.features.orders.tracking

import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.features.orders.model.RealtimeCredentialsResponse
import eti.lucasgomes.makalu.shared.MkLogger
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.network.Resource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface OrderTrackingRepository {
    val statuses: StateFlow<Map<Long, OrderStatus>>

    fun statusOf(orderId: Long): Flow<OrderStatus> =
        statuses.mapNotNull { it[orderId] }.distinctUntilChanged()
}

internal class OrderTracker(
    private val ordersClient: OrdersClient,
    private val realtime: RealtimeClient,
    private val store: TrackedOrdersStore,
    private val clock: () -> Instant,
    private val logger: MkLogger
) : OrderTrackingRepository {
    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TrackingEffect>(extraBufferCapacity = EFFECT_BUFFER)

    val effects: Flow<TrackingEffect> = _effects.asSharedFlow()

    private val _statuses = MutableStateFlow<Map<Long, OrderStatus>>(emptyMap())
    override val statuses: StateFlow<Map<Long, OrderStatus>> = _statuses.asStateFlow()

    private val conflatedRefreshRequests = Channel<Unit>(Channel.CONFLATED)
    private val credentials = mutableMapOf<Long, RealtimeCredentialsResponse>()
    private val orderIdsWithLiveSubscription = mutableSetOf<Long>()
    private var job: Job? = null

    val isRunning: Boolean
        get() = job?.isActive == true

    fun start(scope: CoroutineScope, orderIds: Set<Long>) {
        if (isRunning) {
            orderIds.forEach(::track)
            return
        }
        _state.value = TrackingState(pendingIds = orderIds)
        if (orderIds.isEmpty()) return
        job = scope.launch {
            store.write(orderIds)
            launch { realtime.messages.collect(::onMessage) }
            launch {
                realtime.isConnected.collect { connected ->
                    orderIdsWithLiveSubscription.clear()
                    if (connected) requestRefresh()
                }
            }
            launch {
                while (true) {
                    delay(POLL_INTERVAL)
                    if (!everyFollowedOrderHasLiveSubscription()) requestRefresh()
                }
            }
            requestRefresh()
            for (request in conflatedRefreshRequests) refresh()
        }
    }

    fun track(orderId: Long) {
        apply(OrderTrackingReducer.onTrackRequested(_state.value, orderId))
    }

    fun requestRefresh() {
        conflatedRefreshRequests.trySend(Unit)
    }

    fun stop() {
        job?.cancel()
        job = null
        realtime.disconnect()
        credentials.clear()
        orderIdsWithLiveSubscription.clear()
    }

    private fun everyFollowedOrderHasLiveSubscription(): Boolean = realtime.isConnected.value &&
        orderIdsWithLiveSubscription.containsAll(_state.value.orders.keys)

    private suspend fun refresh() {
        when (val result = ordersClient.getOrders()) {
            is Resource.Success -> {
                apply(OrderTrackingReducer.onSnapshot(_state.value, result.data, clock()))
                store.write(_state.value.idsToPersist)
                syncSubscriptions()
            }

            is Resource.Error -> {
                logger.logDebug(TAG, "could not fetch orders: ${result.error.httpCode}")
                if (result.error.httpCode == HTTP_UNAUTHORIZED) {
                    apply(OrderTrackingReducer.onSessionEnded())
                    store.write(emptySet())
                } else {
                    delay(RETRY_AFTER_FAILURE)
                    requestRefresh()
                }
            }
        }
    }

    private suspend fun syncSubscriptions() {
        val wanted = _state.value.orders.keys

        (credentials.keys - wanted).forEach { orderId ->
            val old = credentials.remove(orderId) ?: return@forEach
            if (orderIdsWithLiveSubscription.remove(
                    orderId
                )
            ) {
                realtime.unsubscribe(old.key, old.channel)
            }
        }

        for (orderId in wanted - orderIdsWithLiveSubscription) {
            val current = credentialsFor(orderId) ?: continue
            realtime.connect(current.url)
            when (realtime.subscribe(current.key, current.channel)) {
                SubscribeResult.Subscribed -> orderIdsWithLiveSubscription += orderId
                SubscribeResult.Refused -> credentials.remove(orderId)
                SubscribeResult.Failed -> Unit
            }
        }
    }

    private suspend fun credentialsFor(orderId: Long): RealtimeCredentialsResponse? {
        credentials[orderId]
            ?.takeIf { it.expiresAt - clock() > RENEW_BEFORE_EXPIRY }
            ?.let { return it }
        return when (val result = ordersClient.getRealtimeCredentials(orderId)) {
            is Resource.Success -> result.data.also { credentials[orderId] = it }
            is Resource.Error -> null
        }
    }

    private fun onMessage(message: RealtimeMessage) {
        apply(OrderTrackingReducer.onEvent(_state.value, parseOrderEvent(message.payload)))
    }

    private fun apply(reduction: Reduction) {
        reduction.effects.forEach { effect ->
            when (effect) {
                TrackingEffect.Refetch -> requestRefresh()

                is TrackingEffect.StatusChanged -> _effects.tryEmit(effect)

                is TrackingEffect.Finished -> {
                    _statuses.update { it + (effect.order.id to effect.order.status) }
                    _effects.tryEmit(effect)
                }
            }
        }
        _statuses.update { known -> known + reduction.state.orders.mapValues { it.value.status } }
        _state.value = reduction.state
    }

    internal companion object {
        const val TAG = "OrderTracker"
        const val EFFECT_BUFFER = 16
        const val HTTP_UNAUTHORIZED = 401
        val POLL_INTERVAL: Duration = 1.minutes
        val RETRY_AFTER_FAILURE: Duration = 15.seconds
        val RENEW_BEFORE_EXPIRY: Duration = 5.minutes
    }
}
