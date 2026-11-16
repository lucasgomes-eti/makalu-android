package eti.lucasgomes.makalu.features.orders.tracking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal data class RealtimeMessage(val channel: String, val payload: String)

internal enum class SubscribeResult {
    Subscribed,

    Refused,

    Failed
}

internal interface RealtimeClient {
    val isConnected: StateFlow<Boolean>

    val messages: Flow<RealtimeMessage>

    fun connect(url: String)

    suspend fun subscribe(key: String, channel: String): SubscribeResult

    suspend fun unsubscribe(key: String, channel: String)

    fun disconnect()
}
