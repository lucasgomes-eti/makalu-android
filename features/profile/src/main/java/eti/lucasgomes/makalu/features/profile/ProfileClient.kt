package eti.lucasgomes.makalu.features.profile

import eti.lucasgomes.makalu.features.profile.model.ProfileResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

class ProfileClient(private val httpClientManager: HttpClientManager) {

    suspend fun getSelfProfile(): Resource<ProfileResponse> = httpClientManager.withApiResource {
        get("profile")
    }
}