package eti.lucasgomes.makalu.features.auth

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.features.auth.login.model.LoginRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.network.TokenPairResponse
import eti.lucasgomes.makalu.shared.settings.Settings
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthClient(
    private val httpClientManager: HttpClientManager,
    private val dataStore: DataStore<Settings>
) {

    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> =
        httpClientManager.withApiResource {
            post("auth/register") { setBody(request) }
        }

    suspend fun login(request: LoginRequest): Resource<TokenPairResponse> =
        httpClientManager.withApiResource(onSuccess = { response ->
            dataStore.updateData { settings ->
                settings.copy(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
            }
            httpClientManager.installAuth()
        }) {
            post("auth/login") { setBody(request) }
        }
}