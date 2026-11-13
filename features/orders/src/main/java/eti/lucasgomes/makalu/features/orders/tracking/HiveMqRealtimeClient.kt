package eti.lucasgomes.makalu.features.orders.tracking

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException
import eti.lucasgomes.makalu.shared.MkLogger
import java.net.URI
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * MQTT 3.1.1 over (secure) WebSocket to the Emitter broker, with the HiveMQ client.
 *
 * Emitter specifics this relies on:
 * - it speaks 3.1.1 only; an MQTT 5 connection is accepted and then every subscribe fails;
 * - the subscription is `<key>/<channel>` but messages arrive on `<channel>`, so they
 *   never match a per-subscription callback and are read from the global stream;
 * - delivery is QoS 0 and nothing is kept for an offline client.
 *
 * Reconnecting is left to the library (exponential backoff, 1 s to 60 s). It holds no
 * wake lock: while the device sleeps, a drop is noticed and repaired when it wakes.
 */
internal class HiveMqRealtimeClient(private val logger: MkLogger) : RealtimeClient {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messages = MutableSharedFlow<RealtimeMessage>(
        extraBufferCapacity = MESSAGE_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val messages: Flow<RealtimeMessage> = _messages.asSharedFlow()

    private var client: Mqtt3AsyncClient? = null

    @Synchronized
    override fun connect(url: String) {
        if (client != null) return
        val uri = URI(url)
        val secure = uri.scheme == "wss"
        val port = if (uri.port != -1) {
            uri.port
        } else if (secure) {
            DEFAULT_TLS_PORT
        } else {
            DEFAULT_PORT
        }

        val builder = MqttClient.builder()
            .useMqttVersion3()
            .identifier("makalu-android-${UUID.randomUUID()}")
            .serverHost(uri.host)
            .serverPort(port)
            .webSocketConfig()
            .serverPath(uri.path.orEmpty().trimStart('/'))
            .applyWebSocketConfig()
            .automaticReconnect()
            .initialDelay(RECONNECT_INITIAL_DELAY_SECONDS, TimeUnit.SECONDS)
            .maxDelay(RECONNECT_MAX_DELAY_SECONDS, TimeUnit.SECONDS)
            .applyAutomaticReconnect()
            .addConnectedListener {
                logger.logDebug(TAG, "connected")
                _isConnected.value = true
            }
            .addDisconnectedListener { context ->
                logger.logDebug(TAG, "disconnected (${context.source})")
                _isConnected.value = false
            }
        val created = (if (secure) builder.sslWithDefaultConfig() else builder).buildAsync()

        created.publishes(MqttGlobalPublishFilter.ALL) { publish ->
            _messages.tryEmit(
                RealtimeMessage(publish.topic.toString(), String(publish.payloadAsBytes))
            )
        }
        created.connectWith()
            .cleanSession(true)
            .keepAlive(KEEP_ALIVE_SECONDS)
            .send()
        client = created
    }

    override suspend fun subscribe(key: String, channel: String): SubscribeResult {
        val current = client ?: return SubscribeResult.Failed
        if (!current.state.isConnected) return SubscribeResult.Failed
        return try {
            val acknowledged = withTimeoutOrNull(REQUEST_TIMEOUT_MILLIS) {
                current.subscribeWith()
                    .topicFilter("$key/$channel")
                    .qos(MqttQos.AT_MOST_ONCE)
                    .send()
                    .await()
            }
            if (acknowledged == null) SubscribeResult.Failed else SubscribeResult.Subscribed
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Mqtt3SubAckException) {
            SubscribeResult.Refused
        } catch (_: Exception) {
            SubscribeResult.Failed
        }
    }

    override suspend fun unsubscribe(key: String, channel: String) {
        val current = client ?: return
        if (!current.state.isConnected) return
        try {
            withTimeoutOrNull(REQUEST_TIMEOUT_MILLIS) {
                current.unsubscribeWith().topicFilter("$key/$channel").send().await()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // Best effort: the subscription dies with the connection anyway.
        }
    }

    @Synchronized
    override fun disconnect() {
        val current = client ?: return
        client = null
        _isConnected.value = false
        if (current.state.isConnectedOrReconnect) current.disconnect()
    }

    private companion object {
        const val TAG = "OrderRealtime"
        const val DEFAULT_TLS_PORT = 443
        const val DEFAULT_PORT = 80
        const val KEEP_ALIVE_SECONDS = 60
        const val RECONNECT_INITIAL_DELAY_SECONDS = 1L
        const val RECONNECT_MAX_DELAY_SECONDS = 60L
        const val REQUEST_TIMEOUT_MILLIS = 10_000L
        const val MESSAGE_BUFFER = 64
    }
}
