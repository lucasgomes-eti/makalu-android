package eti.lucasgomes.makalu.features.orders.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeCredentialsResponse(
    /** `wss://` URL of the broker. */
    val url: String,
    /** Channel path, ending with a slash, e.g. `makalu/order/42/`. */
    val channel: String,
    val key: String,

    @SerialName("expires_at")
    val expiresAt: Instant
) {
    // The key is a credential: keep it out of logs.
    override fun toString(): String =
        "RealtimeCredentialsResponse(url=$url, channel=$channel, expiresAt=$expiresAt)"
}
