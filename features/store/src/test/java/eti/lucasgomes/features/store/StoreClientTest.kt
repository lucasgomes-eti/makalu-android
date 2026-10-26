package eti.lucasgomes.features.store

import eti.lucasgomes.features.store.model.MenuItemResponse
import eti.lucasgomes.features.store.model.MenuItemResponse.Configuration
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.network.unexpectedErrorWithHttpStatusCode
import eti.lucasgomes.makalu.testing.mockHttpClientManager
import eti.lucasgomes.makalu.testing.respondJson
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreClientTest {

    private val storeJson = """
        {
          "id": 42,
          "name": "Pizzeria",
          "categories": [],
          "logo_image_id": 8,
          "cover_image_id": 9,
          "delivery_fee": "5.90"
        }
    """.trimIndent()

    private val errorJson = """
        {"http_code": 404, "message": "Store not found", "internal_code": "MK-404"}
    """.trimIndent()

    @Test
    fun `getStore sends GET to stores path with id`() = runTest {
        var captured: HttpRequestData? = null
        val client = StoreClient(
            mockHttpClientManager { request ->
                captured = request
                respondJson(storeJson)
            }
        )

        client.getStore(42)

        assertEquals(HttpMethod.Get, captured?.method)
        assertEquals("/stores/42", captured?.url?.encodedPath)
    }

    @Test
    fun `getStore decodes store response on success`() = runTest {
        val client = StoreClient(mockHttpClientManager { respondJson(storeJson) })

        val result = client.getStore(42)

        val expected = storeResponse(
            id = 42,
            name = "Pizzeria",
            logoImageId = 8,
            coverImageId = 9,
            deliveryFee = BigDecimal("5.90")
        )
        assertEquals(Resource.Success(expected), result)
    }

    @Test
    fun `getStore decodes null image ids`() = runTest {
        val json = """
            {
              "id": 42,
              "name": "Pizzeria",
              "categories": [],
              "logo_image_id": null,
              "cover_image_id": null,
              "delivery_fee": "0"
            }
        """.trimIndent()
        val client = StoreClient(mockHttpClientManager { respondJson(json) })

        val result = client.getStore(42)

        val store = (result as Resource.Success).data
        assertEquals(null, store.logoImageId)
        assertEquals(null, store.coverImageId)
    }

    @Test
    fun `getStore maps api error body to Resource Error`() = runTest {
        val client = StoreClient(
            mockHttpClientManager {
                respondJson(errorJson, HttpStatusCode.NotFound)
            }
        )

        val result = client.getStore(42)

        assertEquals(Resource.Error(makaluError()), result)
    }

    @Test
    fun `getStore maps undecodable error body to unexpected http error`() = runTest {
        val client = StoreClient(
            mockHttpClientManager {
                respond("Internal error", HttpStatusCode.InternalServerError)
            }
        )

        val result = client.getStore(42)

        assertEquals(Resource.Error(unexpectedErrorWithHttpStatusCode(500)), result)
    }

    @Test
    fun `getStore maps malformed success body to unexpected error`() = runTest {
        val client = StoreClient(mockHttpClientManager { respondJson("{}") })

        val result = client.getStore(42)

        assertTrue(result is Resource.Error)
        val error = (result as Resource.Error).error
        assertEquals(-1, error.httpCode)
        assertEquals("MK-0", error.internalCode)
    }

    @Test
    fun `getMenuItems sends GET to store menu path`() = runTest {
        var captured: HttpRequestData? = null
        val client = StoreClient(
            mockHttpClientManager { request ->
                captured = request
                respondJson("[]")
            }
        )

        client.getMenuItems(7)

        assertEquals(HttpMethod.Get, captured?.method)
        assertEquals("/stores/7/menu", captured?.url?.encodedPath)
    }

    @Test
    fun `getMenuItems decodes menu items with configurations`() = runTest {
        val json = """
            [
              {
                "id": 1,
                "store_id": 7,
                "category": "Pizza",
                "name": "Margherita",
                "price": "12.50",
                "ingredients": "Tomato, mozzarella",
                "image_id": 4,
                "configurations": [
                  {"name": "Size", "type": "SINGLE_CHOICE", "options": [{"name": "Large", "additional_price": "3.00"}]},
                  {"name": "Extras", "type": "MULTIPLE_CHOICE", "options": [{"name": "Olives", "additional_price": "1.5"}]},
                  {"name": "Cheese", "type": "QUANTITY", "options": []}
                ]
              }
            ]
        """.trimIndent()
        val client = StoreClient(mockHttpClientManager { respondJson(json) })

        val result = client.getMenuItems(7)

        val expected = listOf(
            MenuItemResponse(
                id = 1,
                storeId = 7,
                category = "Pizza",
                name = "Margherita",
                price = BigDecimal("12.50"),
                ingredients = "Tomato, mozzarella",
                configurations = listOf(
                    Configuration(
                        "Size",
                        Configuration.Type.SINGLE_CHOICE,
                        listOf(Configuration.Option("Large", BigDecimal("3.00")))
                    ),
                    Configuration(
                        "Extras",
                        Configuration.Type.MULTIPLE_CHOICE,
                        listOf(Configuration.Option("Olives", BigDecimal("1.5")))
                    ),
                    Configuration("Cheese", Configuration.Type.QUANTITY, emptyList())
                ),
                imageId = 4
            )
        )
        assertEquals(Resource.Success(expected), result)
    }

    @Test
    fun `getMenuItems decodes nullable fields as null`() = runTest {
        val json = """
            [
              {
                "id": 1,
                "store_id": 7,
                "category": "Pizza",
                "name": "Margherita",
                "price": "12.50",
                "ingredients": null,
                "image_id": null,
                "configurations": []
              }
            ]
        """.trimIndent()
        val client = StoreClient(mockHttpClientManager { respondJson(json) })

        val result = client.getMenuItems(7)

        val item = (result as Resource.Success).data.single()
        assertEquals(null, item.ingredients)
        assertEquals(null, item.imageId)
        assertEquals(emptyList<Configuration>(), item.configurations)
    }

    @Test
    fun `getMenuItems decodes empty list`() = runTest {
        val client = StoreClient(mockHttpClientManager { respondJson("[]") })

        val result = client.getMenuItems(7)

        assertEquals(Resource.Success(emptyList<MenuItemResponse>()), result)
    }

    @Test
    fun `getMenuItems maps api error body to Resource Error`() = runTest {
        val json = """{"http_code": 400, "message": "Invalid store", "internal_code": "MK-400"}"""
        val client = StoreClient(
            mockHttpClientManager {
                respondJson(json, HttpStatusCode.BadRequest)
            }
        )

        val result = client.getMenuItems(7)

        val expected =
            makaluError(httpCode = 400, message = "Invalid store", internalCode = "MK-400")
        assertEquals(Resource.Error(expected), result)
    }
}
