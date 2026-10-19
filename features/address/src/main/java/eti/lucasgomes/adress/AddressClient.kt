package eti.lucasgomes.adress

import eti.lucasgomes.adress.model.AddressRequest
import eti.lucasgomes.adress.model.ReverseGeocodeResponse
import eti.lucasgomes.makalu.features.adress.BuildConfig
import eti.lucasgomes.makalu.shared.model.AddressResponse
import eti.lucasgomes.makalu.shared.network.EmptyResult
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AddressClient(private val httpClientManager: HttpClientManager) {

    private val geocodingClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        engine {
            connectTimeout = TIME_OUT
            socketTimeout = TIME_OUT
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResponse {
        return try {
            val response = geocodingClient.get("https://maps.googleapis.com/maps/api/geocode/json") {
                parameter("latlng", "$latitude,$longitude")
                parameter("key", BuildConfig.MAPS_API_KEY)
            }.body<GeocodeResponse>()

            val components = response.results.firstOrNull()?.addressComponents
                ?: return ReverseGeocodeResponse()

            fun find(type: String) =
                components.firstOrNull { it.types.contains(type) }?.longName

            ReverseGeocodeResponse(
                zipCode = find("postal_code") ?: "",
                street = find("route") ?: "",
                number = find("street_number") ?: "",
                complement = find("subpremise") ?: ""
            )
        } catch (_: Exception) {
            ReverseGeocodeResponse()
        }

    }

    suspend fun saveAddress(request: AddressRequest): Resource<AddressResponse> =
        httpClientManager.withApiResource {
            post("/addresses") {
                setBody(request)
            }
        }

    suspend fun updateAddress(request: AddressRequest, addressId: Long): EmptyResult =
        httpClientManager.withApiResource {
            put("/addresses/$addressId") {
                setBody(request)
            }
        }

    suspend fun getSelfAddress(): Resource<AddressResponse> = httpClientManager.withApiResource {
        get("/addresses")
    }

    @Serializable
    data class GeocodeResponse(
        val results: List<Result>
    ) {
        @Serializable
        data class Result(
            @SerialName("address_components")
            val addressComponents: List<AddressComponent>
        ) {
            @Serializable
            data class AddressComponent(
                @SerialName("long_name")
                val longName: String,
                val types: List<String>
            )
        }
    }

    companion object {
        private const val TIME_OUT = 6000
    }
}


