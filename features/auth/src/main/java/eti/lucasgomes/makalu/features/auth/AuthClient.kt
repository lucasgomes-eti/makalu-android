package eti.lucasgomes.makalu.features.auth

import eti.lucasgomes.makalu.features.auth.registration.model.RegisterRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthClient(private val httpClientManager: HttpClientManager) {

    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> =
        httpClientManager.withApiResource {
            post("auth/register") { setBody(request) }
        }
}