package eti.lucasgomes.makalu.shared.network

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refreshToken: String)