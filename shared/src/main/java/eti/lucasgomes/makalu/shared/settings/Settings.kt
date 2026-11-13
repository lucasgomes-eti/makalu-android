package eti.lucasgomes.makalu.shared.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val addressId: Long? = null,
    val addressName: String? = null,
    /** Orders the tracking service was following, so it can pick up after process death. */
    val trackedOrderIds: List<Long> = emptyList()
)
