package eti.lucasgomes.features.menuItem

import eti.lucasgomes.features.menuItem.model.CartItemRequest
import eti.lucasgomes.features.menuItem.model.MenuItemResponse
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.mockHttpClientManager
import eti.lucasgomes.makalu.testing.respondJson
import eti.lucasgomes.makalu.testing.testJson
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuItemClientTest {

    private val menuItemJson = """
        {
          "id": 42,
          "store_id": 7,
          "category": "Pizza",
          "name": "Margherita",
          "price": "12.50",
          "ingredients": "Tomato, mozzarella",
          "image_id": 9,
          "configurations": [
            {
              "id": 1,
              "name": "Size",
              "type": "SINGLE_CHOICE",
              "options": [
                { "id": 10, "name": "Large", "additional_price": "3.00" }
              ]
            },
            {
              "id": 2,
              "name": "Extras",
              "type": "MULTIPLE_CHOICE",
              "options": [
                { "id": 20, "name": "Olives", "additional_price": "1.25" }
              ]
            },
            {
              "id": 3,
              "name": "Drinks",
              "type": "QUANTITY",
              "options": [
                { "id": 30, "name": "Soda", "additional_price": "4.5" }
              ]
            }
          ]
        }
    """.trimIndent()

    private val errorJson = """
        { "http_code": 404, "message": "Not found", "internal_code": "MK-404" }
    """.trimIndent()

    @Test
    fun `get item sends GET to the store menu item path`() = runTest {
        var captured: HttpRequestData? = null
        val client = MenuItemClient(
            mockHttpClientManager { request ->
                captured = request
                respondJson(menuItemJson)
            }
        )

        client.getItem(42)

        assertEquals(HttpMethod.Get, captured?.method)
        assertEquals("/stores/menu/42", captured?.url?.encodedPath)
    }

    @Test
    fun `get item decodes the menu item response`() = runTest {
        val client = MenuItemClient(mockHttpClientManager { respondJson(menuItemJson) })

        val result = client.getItem(42)

        val expected = MenuItemResponse(
            id = 42,
            storeId = 7,
            category = "Pizza",
            name = "Margherita",
            price = BigDecimal("12.50"),
            ingredients = "Tomato, mozzarella",
            configurations = listOf(
                MenuItemResponse.Configuration(
                    1,
                    "Size",
                    MenuItemResponse.Configuration.Type.SINGLE_CHOICE,
                    listOf(MenuItemResponse.Configuration.Option(10, "Large", BigDecimal("3.00")))
                ),
                MenuItemResponse.Configuration(
                    2,
                    "Extras",
                    MenuItemResponse.Configuration.Type.MULTIPLE_CHOICE,
                    listOf(MenuItemResponse.Configuration.Option(20, "Olives", BigDecimal("1.25")))
                ),
                MenuItemResponse.Configuration(
                    3,
                    "Drinks",
                    MenuItemResponse.Configuration.Type.QUANTITY,
                    listOf(MenuItemResponse.Configuration.Option(30, "Soda", BigDecimal("4.5")))
                )
            ),
            imageId = 9
        )
        assertEquals(Resource.Success(expected), result)
    }

    @Test
    fun `get item decodes null ingredients and image id`() = runTest {
        val json = """
            {
              "id": 1, "store_id": 2, "category": "c", "name": "n", "price": "1.00",
              "ingredients": null, "image_id": null, "configurations": []
            }
        """.trimIndent()
        val client = MenuItemClient(mockHttpClientManager { respondJson(json) })

        val result = client.getItem(1) as Resource.Success

        assertNull(result.data.ingredients)
        assertNull(result.data.imageId)
        assertTrue(result.data.configurations.isEmpty())
    }

    @Test
    fun `get item maps an error response to Resource Error`() = runTest {
        val client = MenuItemClient(
            mockHttpClientManager {
                respondJson(errorJson, HttpStatusCode.NotFound)
            }
        )

        val result = client.getItem(42)

        assertEquals(Resource.Error(MakaluError(404, "Not found", "MK-404")), result)
    }

    @Test
    fun `get item returns an unexpected error when the success body is malformed`() = runTest {
        val client = MenuItemClient(mockHttpClientManager { respondJson("{}") })

        val result = client.getItem(42)

        assertEquals("MK-0", (result as Resource.Error).error.internalCode)
    }

    @Test
    fun `add item to cart sends PUT to the store menu item cart path`() = runTest {
        var captured: HttpRequestData? = null
        val client = MenuItemClient(
            mockHttpClientManager { request ->
                captured = request
                respond("", HttpStatusCode.OK)
            }
        )

        client.addItemToCart(7, 42, CartItemRequest(emptyList()))

        assertEquals(HttpMethod.Put, captured?.method)
        assertEquals("/stores/7/menu/42/cart", captured?.url?.encodedPath)
    }

    @Test
    fun `add item to cart serializes configurations and notes`() = runTest {
        var body: String? = null
        val client = MenuItemClient(
            mockHttpClientManager { request ->
                body = (request.body as TextContent).text
                respond("", HttpStatusCode.OK)
            }
        )
        val request = CartItemRequest(
            configurations = listOf(CartItemRequest.Configuration(5, 3)),
            notes = "No onions"
        )

        client.addItemToCart(7, 42, request)

        val json = testJson.parseToJsonElement(body!!) as JsonObject
        val configuration = json.getValue("configurations").jsonArray.single().jsonObject
        assertEquals(
            5L,
            configuration.getValue("menu_item_configuration_option_id").jsonPrimitive.long
        )
        assertEquals(3, configuration.getValue("quantity").jsonPrimitive.int)
        assertEquals("No onions", json.getValue("notes").jsonPrimitive.content)
    }

    @Test
    fun `add item to cart returns success on 2xx`() = runTest {
        val client = MenuItemClient(mockHttpClientManager { respond("", HttpStatusCode.OK) })

        val result = client.addItemToCart(7, 42, CartItemRequest(emptyList()))

        assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun `add item to cart maps an error response to Resource Error`() = runTest {
        val json = """
            { "http_code": 400, "message": "Out of stock", "internal_code": "MK-2" }
        """.trimIndent()
        val client = MenuItemClient(
            mockHttpClientManager {
                respondJson(json, HttpStatusCode.BadRequest)
            }
        )

        val result = client.addItemToCart(7, 42, CartItemRequest(emptyList()))

        assertEquals(Resource.Error(MakaluError(400, "Out of stock", "MK-2")), result)
    }
}
