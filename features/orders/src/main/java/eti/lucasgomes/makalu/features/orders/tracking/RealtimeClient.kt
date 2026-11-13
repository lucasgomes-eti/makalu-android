package eti.lucasgomes.makalu.features.orders.tracking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal data class RealtimeMessage(val channel: String, val payload: String)

internal enum class SubscribeResult {
    Subscribed,

    /** The broker refused the key: it expired or was revoked. */
    Refused,

    /** Not connected, or the broker did not answer. */
    Failed
}

/** The broker as the tracking logic sees it. Tests substitute a fake. */
internal interface RealtimeClient {

    /** Whether a connection is currently established. */
    val isConnected: StateFlow<Boolean>

    /** Messages from every subscribed channel. */
    val messages: Flow<RealtimeMessage>

    /** Connects to [url] and keeps reconnecting with backoff. Does nothing if already started. */
    fun connect(url: String)

    suspend fun subscribe(key: String, channel: String): SubscribeResult

    suspend fun unsubscribe(key: String, channel: String)

    /** Closes the connection; [connect] may be called again afterwards. */
    fun disconnect()
}
