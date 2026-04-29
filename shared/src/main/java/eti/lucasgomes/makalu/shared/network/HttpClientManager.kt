package eti.lucasgomes.makalu.shared.network

import MakaluConfig
import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.MkLogger
import eti.lucasgomes.makalu.shared.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.clearAuthTokens
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class HttpClientManager(
    private val mkLogger: MkLogger,
    private val dataStore: DataStore<Settings>,
    private val logoutUseCase: LogoutUseCase
) {

    private var _httpClient: HttpClient? = null

    fun getClient(): HttpClient {
        return _httpClient ?: createHttpClient().also { _httpClient = it }
    }

    private fun createHttpClient(): HttpClient {
        return HttpClient(Android) {
            defaultRequest {
                url(MakaluConfig.BASE_URL)
            }
            val contentSerializer = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
            }
            install(ContentNegotiation) {
                json(contentSerializer)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        mkLogger.logDebug("HttpLogging", message)
                    }
                }
            }
            engine {
                connectTimeout = TIME_OUT
                socketTimeout = TIME_OUT
            }
            install(ResponseObserver) {
                onResponse { response ->
                    mkLogger.logDebug("HTTP status", "${response.status.value}")
                }
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val (accessToken, refreshToken) = dataStore.data.first()
                        accessToken?.let { BearerTokens(it, refreshToken) }
                    }
                    refreshTokens {
                        val refreshToken = oldTokens?.refreshToken ?: run {
                            client.clearAuthTokens()
                            logoutUseCase()
                            return@refreshTokens null
                        }
                        val response = client.post("auth/refresh") {
                            setBody(RefreshTokenRequest(refreshToken))
                            markAsRefreshTokenRequest()
                        }

                        val auth = when (response.status.value) {
                            in 200..299 -> {
                                response.body<TokenPairResponse>().also { auth ->
                                    dataStore.updateData { settings ->
                                        settings.copy(
                                            accessToken = auth.accessToken,
                                            refreshToken = auth.refreshToken
                                        )
                                    }
                                }
                            }

                            else -> {
                                client.clearAuthTokens()
                                logoutUseCase()
                                null
                            }
                        }

                        auth?.let {
                            BearerTokens(it.accessToken, it.refreshToken)
                        }
                    }
                }
            }
        }
    }

    fun installAuth() {
        refreshHttpClient()
    }

    private fun refreshHttpClient() {
        _httpClient?.close()
        _httpClient = createHttpClient()
    }

    suspend inline fun <reified T> withApiResource(
        noinline onSuccess: (suspend (T) -> Unit)? = null,
        noinline onFailure: (suspend (MakaluError) -> Unit)? = null,
        httpRequest: HttpClient.() -> HttpResponse
    ): Resource<T> {
        val response = try {
            getClient().httpRequest()
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            return Resource.Error(unexpectedErrorWithException(e))
        }

        return when (response.status.value) {
            in 200..299 -> {
                val data = response.body<T>()
                onSuccess?.invoke(data)
                Resource.Success(data)
            }

            in 400..599 -> {
                val error = try {
                    response.body<MakaluError>()
                } catch (_: Exception) {
                    unexpectedErrorWithHttpStatusCode(response.status.value)
                }
                onFailure?.invoke(error)
                Resource.Error(error)
            }

            else -> {
                val error = unexpectedErrorWithHttpStatusCode(response.status.value)
                onFailure?.invoke(error)
                Resource.Error(error)
            }
        }
    }

    companion object {
        private const val TIME_OUT = 6000
    }
}