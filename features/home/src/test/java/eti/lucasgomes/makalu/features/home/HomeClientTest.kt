package eti.lucasgomes.makalu.features.home

import eti.lucasgomes.makalu.shared.model.AddressResponse
import eti.lucasgomes.makalu.shared.model.CategoryResponse
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.network.unexpectedErrorWithHttpStatusCode
import eti.lucasgomes.makalu.testing.mockHttpClientManager
import eti.lucasgomes.makalu.testing.respondJson
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

class HomeClientTest {

    private var captured: HttpRequestData? = null

    private fun client(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        HomeClient(mockHttpClientManager { request ->
            captured = request
            respondJson(body, status)
        })

    private val errorJson =
        """{"http_code":404,"message":"Address not found","internal_code":"MK-404"}"""

    private val error = MakaluError(httpCode = 404, message = "Address not found", internalCode = "MK-404")

    @Test
    fun `getCategories sends GET to categories path`() = runTest {
        client("[]").getCategories()

        assertEquals(HttpMethod.Get, captured!!.method)
        assertEquals("/categories", captured!!.url.encodedPath)
    }

    @Test
    fun `getCategories decodes category list`() = runTest {
        val body = """[{"id":1,"description":"Pizza"},{"id":2,"description":"Coffee"}]"""

        val result = client(body).getCategories()

        assertEquals(
            Resource.Success(listOf(CategoryResponse(1, "Pizza"), CategoryResponse(2, "Coffee"))),
            result
        )
    }

    @Test
    fun `getCategories maps api error body to Resource Error`() = runTest {
        val result = client(errorJson, HttpStatusCode.InternalServerError).getCategories()

        assertEquals(Resource.Error(error), result)
    }

    @Test
    fun `getStores without categories sends GET to stores path without categories parameter`() = runTest {
        client("[]").getStores()

        assertEquals(HttpMethod.Get, captured!!.method)
        assertEquals("/stores", captured!!.url.encodedPath)
        assertNull(captured!!.url.parameters["categories"])
    }

    @Test
    fun `getStores with categories sends comma separated categories parameter`() = runTest {
        client("[]").getStores(listOf(1L, 2L))

        assertEquals("/stores", captured!!.url.encodedPath)
        assertEquals("1,2", captured!!.url.parameters["categories"])
    }

    @Test
    fun `getStores decodes snake case store fields`() = runTest {
        val body = """
            [{
              "id": 7,
              "name": "Scary's",
              "categories": [{"id": 1, "description": "Burger"}],
              "logo_image_id": 3,
              "cover_image_id": null,
              "delivery_fee": "4.90"
            }]
        """.trimIndent()

        val result = client(body).getStores()

        val store = (result as Resource.Success).data.single()
        assertEquals(7L, store.id)
        assertEquals("Scary's", store.name)
        assertEquals(listOf(CategoryResponse(1, "Burger")), store.categories)
        assertEquals(3L, store.logoImageId)
        assertNull(store.coverImageId)
        assertEquals(BigDecimal("4.90"), store.deliveryFee)
    }

    @Test
    fun `getStores with undecodable error body returns unexpected http error`() = runTest {
        val result = client("not json", HttpStatusCode.ServiceUnavailable).getStores()

        assertEquals(Resource.Error(unexpectedErrorWithHttpStatusCode(503)), result)
    }

    @Test
    fun `getSelfAddress sends GET to addresses path and decodes address`() = runTest {
        val body = """
            {
              "id": 5,
              "zip_code": "12345-000",
              "street": "Main St",
              "number": null,
              "complement": null,
              "longitude": -46.6,
              "latitude": -23.5
            }
        """.trimIndent()

        val result = client(body).getSelfAddress()

        assertEquals(HttpMethod.Get, captured!!.method)
        assertEquals("/addresses", captured!!.url.encodedPath)
        assertEquals(
            Resource.Success(AddressResponse(5, "12345-000", "Main St", null, null, -46.6, -23.5)),
            result
        )
    }

    @Test
    fun `getSelfAddress returns error when address is not found`() = runTest {
        val result = client(errorJson, HttpStatusCode.NotFound).getSelfAddress()

        assertEquals(Resource.Error(error), result)
    }

    @Test
    fun `getSelfAddress returns unexpected error when request throws`() = runTest {
        val homeClient = HomeClient(mockHttpClientManager { throw IOException("boom") })

        val result = homeClient.getSelfAddress()

        val resultError = (result as Resource.Error).error
        assertEquals(-1, resultError.httpCode)
        assertEquals("MK-0", resultError.internalCode)
        assertTrue(resultError.message.contains("boom"))
    }
}
