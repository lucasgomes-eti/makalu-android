package eti.lucasgomes.makalu.features.orders.tracking

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.features.orders.model.RealtimeCredentialsResponse
import eti.lucasgomes.makalu.shared.MkLogger
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal val NOW: Instant = Instant.parse("2026-01-10T12:35:00Z")

internal val noLogger = object : MkLogger {
    override fun logDebug(tag: String, message: String) = Unit
}

internal class InMemorySettings(initial: Settings = Settings()) : DataStore<Settings> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Settings> = state
    val value: Settings get() = state.value

    override suspend fun updateData(transform: suspend (t: Settings) -> Settings): Settings {
        state.update { transform(it) }
        return state.value
    }
}

internal fun credentials(
    orderId: Long,
    key: String = "key-$orderId",
    expiresAt: Instant = NOW +
        24.hours
) = RealtimeCredentialsResponse(
    url = "wss://broker.test/",
    channel = "makalu/order/$orderId/",
    key = key,
    expiresAt = expiresAt
)

/** Stands in for the broker: the test plays its part. */
internal class FakeRealtimeClient : RealtimeClient {
    override val isConnected = MutableStateFlow(false)
    override val messages = MutableSharedFlow<RealtimeMessage>(extraBufferCapacity = 16)

    val connectedTo = mutableListOf<String>()
    val subscriptions = mutableListOf<String>()
    val unsubscriptions = mutableListOf<String>()
    var disconnects = 0

    /** Keys the broker refuses. */
    val refusedKeys = mutableSetOf<String>()

    override fun connect(url: String) {
        connectedTo += url
    }

    override suspend fun subscribe(key: String, channel: String): SubscribeResult = when {
        !isConnected.value -> SubscribeResult.Failed
        key in refusedKeys -> SubscribeResult.Refused
        else -> SubscribeResult.Subscribed.also { subscriptions += "$key/$channel" }
    }

    override suspend fun unsubscribe(key: String, channel: String) {
        unsubscriptions += "$key/$channel"
    }

    override fun disconnect() {
        disconnects += 1
        isConnected.value = false
    }

    fun deliver(
        orderId: Long,
        status: String,
        updatedAt: String,
        type: String = "order.status_changed"
    ) {
        messages.tryEmit(
            RealtimeMessage(
                "makalu/order/$orderId/",
                """{"v":1,"type":"$type","orderId":"$orderId","storeId":"7",""" +
                    """"status":"$status","updatedAt":"$updatedAt"}"""
            )
        )
    }
}
