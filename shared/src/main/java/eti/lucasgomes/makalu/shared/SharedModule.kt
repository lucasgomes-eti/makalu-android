package eti.lucasgomes.makalu.shared

import eti.lucasgomes.makalu.shared.navigation.DefaultNavigator
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val BASE_URL: String = "https://makalu-development.up.railway.app/"
private const val TIME_OUT = 6000

val sharedModule = module {
    single<Navigator> {
        DefaultNavigator(startDestination = Destination.Graph.Auth)
    }
    single<HttpClient> {
        val mkLogger: MkLogger = get()
        HttpClient(Android) {
            defaultRequest {
                url(BASE_URL)
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
        }
    }
}