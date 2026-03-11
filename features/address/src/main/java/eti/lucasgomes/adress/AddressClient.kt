package eti.lucasgomes.adress

import eti.lucasgomes.adress.model.AddressRequest
import eti.lucasgomes.adress.model.AddressResponse
import eti.lucasgomes.adress.model.ReverseGeocodeResponse
import eti.lucasgomes.makalu.features.adress.BuildConfig
import eti.lucasgomes.makalu.shared.network.EmptyResult
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AddressClient(private val httpClientManager: HttpClientManager) {

    suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResponse {
        return try {
            val response = httpClientManager.getClient().get {
                url("https://maps.googleapis.com/maps/api/geocode/json")
                headers.remove(HttpHeaders.Authorization)
                parameter("latlng", "$latitude,$longitude")
                parameter("key", BuildConfig.MAPS_API_KEY)
            }.body<GeocodeResponse>()

            val components = response.results.first().addressComponents

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

}


