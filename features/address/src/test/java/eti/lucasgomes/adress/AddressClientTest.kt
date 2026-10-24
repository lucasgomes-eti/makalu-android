package eti.lucasgomes.adress

import eti.lucasgomes.adress.model.AddressRequest
import eti.lucasgomes.adress.model.ReverseGeocodeResponse
import eti.lucasgomes.makalu.features.adress.BuildConfig
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.mockHttpClient
import eti.lucasgomes.makalu.testing.mockHttpClientManager
import eti.lucasgomes.makalu.testing.respondJson
import eti.lucasgomes.makalu.testing.testJson
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AddressClientTest {

    private val request = AddressRequest(
        zipCode = "01310100",
        street = "Rua A",
        number = "42",
        complement = "Apt 1",
        longitude = -46.6,
        latitude = -23.5
    )

    private val addressJson = """
        {
          "id": 7,
          "zip_code": "01310100",
          "street": "Av. Paulista",
          "number": "1000",
          "complement": "Apt 12",
          "longitude": -46.65,
          "latitude": -23.56
        }
    """.trimIndent()

    private val errorJson = """{"http_code": 404, "message": "Not found", "internal_code": "MK-404"}"""

    private fun geocodeClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = AddressClient(mockk(relaxed = true)).withGeocodingClient(mockHttpClient(handler))

    private fun component(longName: String, vararg types: String) =
        """{"long_name": "$longName", "types": [${types.joinToString { "\"$it\"" }}]}"""

    private fun geocodeJson(vararg results: List<String>) =
        """{"results": [${results.joinToString { """{"address_components": [${it.joinToString()}]}""" }}]}"""

    private fun HttpRequestData.jsonBody(): JsonObject =
        testJson.parseToJsonElement((body as TextContent).text).jsonObject

    private fun assertRequestBody(body: JsonObject) {
        assertEquals(JsonPrimitive("01310100"), body["zip_code"])
        assertEquals(JsonPrimitive("Rua A"), body["street"])
        assertEquals(JsonPrimitive("42"), body["number"])
        assertEquals(JsonPrimitive("Apt 1"), body["complement"])
        assertEquals(JsonPrimitive(-46.6), body["longitude"])
        assertEquals(JsonPrimitive(-23.5), body["latitude"])
    }

    @Test
    fun `reverse geocode calls the google geocode api with latlng and api key`() = runTest {
        var captured: HttpRequestData? = null
        val client = geocodeClient {
            captured = it
            respondJson(geocodeJson())
        }

        client.reverseGeocode(-23.5, -46.6)

        val sent = captured!!
        assertEquals(HttpMethod.Get, sent.method)
        assertEquals("maps.googleapis.com", sent.url.host)
        assertEquals("/maps/api/geocode/json", sent.url.encodedPath)
        assertEquals("-23.5,-46.6", sent.url.parameters["latlng"])
        assertEquals(BuildConfig.MAPS_API_KEY, sent.url.parameters["key"])
    }

    @Test
    fun `reverse geocode maps postal code route street number and subpremise`() = runTest {
        val client = geocodeClient {
            respondJson(
                geocodeJson(
                    listOf(
                        component("1000", "street_number"),
                        component("Apt 12", "subpremise"),
                        component("Av. Paulista", "route"),
                        component("01310100", "postal_code"),
                    )
                )
            )
        }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals(ReverseGeocodeResponse("01310100", "Av. Paulista", "1000", "Apt 12"), response)
    }

    @Test
    fun `reverse geocode reads only the first result`() = runTest {
        val client = geocodeClient {
            respondJson(
                geocodeJson(
                    listOf(component("Rua A", "route"), component("11111111", "postal_code")),
                    listOf(component("Rua B", "route"), component("22222222", "postal_code")),
                )
            )
        }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals("Rua A", response.street)
        assertEquals("11111111", response.zipCode)
    }

    @Test
    fun `reverse geocode matches components carrying several types`() = runTest {
        val client = geocodeClient {
            respondJson(geocodeJson(listOf(component("Rua A", "political", "route"))))
        }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals("Rua A", response.street)
    }

    @Test
    fun `reverse geocode leaves missing components empty`() = runTest {
        val client = geocodeClient {
            respondJson(geocodeJson(listOf(component("Rua A", "route"))))
        }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals(ReverseGeocodeResponse(street = "Rua A"), response)
    }

    @Test
    fun `reverse geocode ignores unknown keys`() = runTest {
        val client = geocodeClient {
            respondJson(
                """
                {
                  "status": "OK",
                  "results": [{
                    "formatted_address": "Av. Paulista, 1000",
                    "address_components": [{"long_name": "Av. Paulista", "short_name": "Av. Paulista", "types": ["route"]}]
                  }]
                }
                """.trimIndent()
            )
        }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals("Av. Paulista", response.street)
    }

    @Test
    fun `reverse geocode without results returns an empty response`() = runTest {
        val client = geocodeClient { respondJson("""{"results": []}""") }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals(ReverseGeocodeResponse(), response)
    }

    @Test
    fun `reverse geocode with a malformed body returns an empty response`() = runTest {
        val client = geocodeClient { respondJson("""{"error_message": "denied"}""") }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals(ReverseGeocodeResponse(), response)
    }

    @Test
    fun `reverse geocode returns an empty response when the request fails`() = runTest {
        val client = geocodeClient { throw IOException("offline") }

        val response = client.reverseGeocode(-23.5, -46.6)

        assertEquals(ReverseGeocodeResponse(), response)
    }

    @Test
    fun `save address posts the request as json to addresses`() = runTest {
        var captured: HttpRequestData? = null
        val client = AddressClient(mockHttpClientManager {
            captured = it
            respondJson(addressJson, HttpStatusCode.Created)
        })

        client.saveAddress(request)

        assertEquals(HttpMethod.Post, captured!!.method)
        assertEquals("/addresses", captured!!.url.encodedPath)
        assertRequestBody(captured!!.jsonBody())
    }

    @Test
    fun `save address decodes the created address`() = runTest {
        val client = AddressClient(mockHttpClientManager { respondJson(addressJson, HttpStatusCode.Created) })

        val result = client.saveAddress(request)

        assertEquals(Resource.Success(addressResponse()), result)
    }

    @Test
    fun `save address maps an error response to resource error`() = runTest {
        val client = AddressClient(mockHttpClientManager {
            respondJson(
                """{"http_code": 400, "message": "Invalid", "internal_code": "MK-1"}""",
                HttpStatusCode.BadRequest
            )
        })

        val result = client.saveAddress(request)

        assertEquals(Resource.Error(makaluError(400, "Invalid", "MK-1")), result)
    }

    @Test
    fun `update address puts the request to the address id path`() = runTest {
        var captured: HttpRequestData? = null
        val client = AddressClient(mockHttpClientManager {
            captured = it
            respond("", HttpStatusCode.NoContent)
        })

        client.updateAddress(request, 7)

        assertEquals(HttpMethod.Put, captured!!.method)
        assertEquals("/addresses/7", captured!!.url.encodedPath)
        assertRequestBody(captured!!.jsonBody())
    }

    @Test
    fun `update address returns success on an empty 2xx response`() = runTest {
        val client = AddressClient(mockHttpClientManager { respond("", HttpStatusCode.NoContent) })

        val result = client.updateAddress(request, 7)

        assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun `update address maps an error response to resource error`() = runTest {
        val client = AddressClient(mockHttpClientManager { respondJson(errorJson, HttpStatusCode.NotFound) })

        val result = client.updateAddress(request, 7)

        assertEquals(Resource.Error(makaluError(404, "Not found", "MK-404")), result)
    }

    @Test
    fun `get self address sends get to addresses`() = runTest {
        var captured: HttpRequestData? = null
        val client = AddressClient(mockHttpClientManager {
            captured = it
            respondJson(addressJson)
        })

        client.getSelfAddress()

        assertEquals(HttpMethod.Get, captured!!.method)
        assertEquals("/addresses", captured!!.url.encodedPath)
    }

    @Test
    fun `get self address decodes snake case fields and null optionals`() = runTest {
        val client = AddressClient(mockHttpClientManager {
            respondJson(
                """
                {
                  "id": 3,
                  "zip_code": "04538133",
                  "street": "Rua B",
                  "number": null,
                  "complement": null,
                  "longitude": -46.68,
                  "latitude": -23.58
                }
                """.trimIndent()
            )
        })

        val result = client.getSelfAddress()

        val expected = addressResponse(
            id = 3,
            zipCode = "04538133",
            street = "Rua B",
            number = null,
            complement = null,
            latitude = -23.58,
            longitude = -46.68
        )
        assertEquals(Resource.Success(expected), result)
    }

    @Test
    fun `get self address maps not found to resource error`() = runTest {
        val client = AddressClient(mockHttpClientManager { respondJson(errorJson, HttpStatusCode.NotFound) })

        val result = client.getSelfAddress()

        assertEquals(Resource.Error(makaluError(404, "Not found", "MK-404")), result)
    }
}
