package eti.lucasgomes.makalu.features.orders.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeCredentialsResponse(
    val url: String,
    val channel: String,
    val key: String,

    @SerialName("expires_at")
    val expiresAt: Instant
) {
    override fun toString(): String =
        "RealtimeCredentialsResponse(url=$url, channel=$channel, expiresAt=$expiresAt)"
}
