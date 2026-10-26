package eti.lucasgomes.features.cart

import eti.lucasgomes.features.cart.model.CartResponse
import eti.lucasgomes.features.cart.model.CreateOrderRequest
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.mockHttpClientManager
import eti.lucasgomes.makalu.testing.respondJson
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import java.math.BigDecimal
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class CartClientTest {

    private val cartJson = """
        {
          "id": 42,
          "delivery_address_line": null,
          "items": [
            {
              "id": 1,
              "image_id": null,
              "name": "Pizza",
              "price": "10.50",
              "notes": null,
              "configurations": [
                {
                  "configuration_id": 10,
                  "name": "Size",
                  "options": [
                    { "id": 11, "name": "Large", "additional_price": "2.50", "quantity": 2 }
                  ]
                }
              ]
            }
          ],
          "total": { "delivery_fee": "5.00", "total": "15.50" }
        }
    """.trimIndent()

    private val expectedCart = CartResponse(
        id = 42L,
        deliveryAddressLine = null,
        items = listOf(
            CartResponse.CartItemResponse(
                id = 1L,
                imageId = null,
                name = "Pizza",
                price = BigDecimal("10.50"),
                notes = null,
                configurations = listOf(
                    CartResponse.CartItemResponse.Configuration(
                        configurationId = 10L,
                        name = "Size",
                        options = listOf(
                            CartResponse.CartItemResponse.Configuration.Option(
                                id = 11L,
                                name = "Large",
                                additionalPrice = BigDecimal("2.50"),
                                quantity = 2
                            )
                        )
                    )
                )
            )
        ),
        total = CartResponse.Total(deliveryFee = BigDecimal("5.00"), total = BigDecimal("15.50"))
    )

    private val orderJson = """
        {
          "id": 77,
          "status": "PENDING",
          "order_number": "0001",
          "store_name": "Store",
          "store_image_id": null,
          "delivery_address_line": "Rua A, 10",
          "items_count": 1,
          "total": { "delivery_fee": "5.00", "total": "15.50" },
          "created_at": "2026-01-02T10:00:00Z",
          "updated_at": "2026-01-02T11:00:00Z"
        }
    """.trimIndent()

    private val errorJson = """
        { "http_code": 404, "message": "Cart not found", "internal_code": "MK-404" }
    """.trimIndent()

    private val expectedError =
        MakaluError(httpCode = 404, message = "Cart not found", internalCode = "MK-404")

    private fun clientRespondingJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: (HttpRequestData) -> Unit = {}
    ) = CartClient(
        mockHttpClientManager { request ->
            onRequest(request)
            respondJson(body, status)
        }
    )

    @Test
    fun `getByStore sends a get to the store cart path`() = runTest {
        var captured: HttpRequestData? = null
        val client = clientRespondingJson(cartJson) { captured = it }

        client.getByStore(STORE_ID)

        assertEquals(HttpMethod.Get, captured!!.method)
        assertEquals("/stores/5/cart", captured!!.url.encodedPath)
    }

    @Test
    fun `getByStore decodes the cart response`() = runTest {
        val client = clientRespondingJson(cartJson)

        val result = client.getByStore(STORE_ID)

        assertEquals(Resource.Success(expectedCart), result)
    }

    @Test
    fun `getByStore maps an error response to a resource error`() = runTest {
        val client = clientRespondingJson(errorJson, HttpStatusCode.NotFound)

        val result = client.getByStore(STORE_ID)

        assertEquals(Resource.Error(expectedError), result)
    }

    @Test
    fun `deleteByItem sends a delete to the cart item path`() = runTest {
        var captured: HttpRequestData? = null
        val client = clientRespondingJson(cartJson) { captured = it }

        client.deleteByItem(STORE_ID, 9L)

        assertEquals(HttpMethod.Delete, captured!!.method)
        assertEquals("/stores/5/cart/items/9", captured!!.url.encodedPath)
    }

    @Test
    fun `deleteByItem decodes the updated cart`() = runTest {
        val client = clientRespondingJson(cartJson)

        val result = client.deleteByItem(STORE_ID, 9L)

        assertEquals(Resource.Success(expectedCart), result)
    }

    @Test
    fun `deleteByItem maps an error response to a resource error`() = runTest {
        val client = clientRespondingJson(errorJson, HttpStatusCode.NotFound)

        val result = client.deleteByItem(STORE_ID, 9L)

        assertEquals(Resource.Error(expectedError), result)
    }

    @Test
    fun `deleteAllItemsByStore sends a delete to the store cart items path`() = runTest {
        var captured: HttpRequestData? = null
        val client = CartClient(
            mockHttpClientManager { request ->
                captured = request
                respond("", HttpStatusCode.NoContent)
            }
        )

        client.deleteAllItemsByStore(STORE_ID)

        assertEquals(HttpMethod.Delete, captured!!.method)
        assertEquals("/stores/5/cart/items", captured!!.url.encodedPath)
    }

    @Test
    fun `deleteAllItemsByStore returns success on a no content response`() = runTest {
        val client = CartClient(mockHttpClientManager { respond("", HttpStatusCode.NoContent) })

        val result = client.deleteAllItemsByStore(STORE_ID)

        assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun `deleteAllItemsByStore maps an error response to a resource error`() = runTest {
        val client = clientRespondingJson(errorJson, HttpStatusCode.NotFound)

        val result = client.deleteAllItemsByStore(STORE_ID)

        assertEquals(Resource.Error(expectedError), result)
    }

    @Test
    fun `submitOrder posts the store and cart ids to orders`() = runTest {
        var captured: HttpRequestData? = null
        val client = clientRespondingJson(orderJson, HttpStatusCode.Created) { captured = it }

        client.submitOrder(CreateOrderRequest(storeId = STORE_ID, cartId = 42L))

        assertEquals(HttpMethod.Post, captured!!.method)
        assertEquals("/orders", captured!!.url.encodedPath)
        val body = Json.parseToJsonElement((captured!!.body as TextContent).text)
        assertEquals(
            JsonObject(mapOf("store_id" to JsonPrimitive(5), "cart_id" to JsonPrimitive(42))),
            body
        )
    }

    @Test
    fun `submitOrder decodes the created order`() = runTest {
        val client = clientRespondingJson(orderJson, HttpStatusCode.Created)

        val result = client.submitOrder(CreateOrderRequest(storeId = STORE_ID, cartId = 42L))

        val expected = OrderSimpleResponse(
            id = 77L,
            status = OrderStatus.PENDING,
            orderNumber = "0001",
            storeName = "Store",
            storeImageId = null,
            deliveryAddressLine = "Rua A, 10",
            itemsCount = 1,
            total = OrderSimpleResponse.Total(BigDecimal("5.00"), BigDecimal("15.50")),
            createdAt = Instant.parse("2026-01-02T10:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T11:00:00Z")
        )
        assertEquals(Resource.Success(expected), result)
    }

    @Test
    fun `submitOrder maps an error response to a resource error`() = runTest {
        val client = clientRespondingJson(errorJson, HttpStatusCode.NotFound)

        val result = client.submitOrder(CreateOrderRequest(storeId = STORE_ID, cartId = 42L))

        assertEquals(Resource.Error(expectedError), result)
    }
}
