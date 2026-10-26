package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.network.MakaluError
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdersClientTest {

    private var capturedRequest: HttpRequestData? = null

    private fun clientResponding(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        OrdersClient(
            mockHttpClientManager { request ->
                capturedRequest = request
                respondJson(body, status)
            }
        )

    private fun <T> Resource<T>.successData(): T = (this as Resource.Success).data

    @Test
    fun `getOrders sends GET to orders path`() = runTest {
        val client = clientResponding("[]")

        client.getOrders()

        assertEquals(HttpMethod.Get, capturedRequest?.method)
        assertEquals("/orders", capturedRequest?.url?.encodedPath)
    }

    @Test
    fun `getOrders decodes the order list on success`() = runTest {
        val client =
            clientResponding(
                "[${orderSimpleJson(id = 1)}, ${orderSimpleJson(id = 2, status = "IN_ROUTE")}]"
            )

        val result = client.getOrders()

        assertEquals(
            Resource.Success(
                listOf(
                    orderSimpleResponse(id = 1),
                    orderSimpleResponse(id = 2, status = OrderStatus.IN_ROUTE)
                )
            ),
            result
        )
    }

    @Test
    fun `getOrders decodes a null store image id`() = runTest {
        val client = clientResponding("[${orderSimpleJson(storeImageId = "null")}]")

        val result = client.getOrders()

        assertNull(result.successData().single().storeImageId)
    }

    @Test
    fun `getOrders keeps the money scale from the payload`() = runTest {
        val client = clientResponding("[${orderSimpleJson(total = "24.50")}]")

        val total = client.getOrders().successData().single().total.total

        assertEquals(BigDecimal("24.50"), total)
        assertEquals(2, total.scale())
    }

    @Test
    fun `getOrders decodes an empty list`() = runTest {
        val client = clientResponding("[]")

        val result = client.getOrders()

        assertEquals(Resource.Success(emptyList<OrderSimpleResponse>()), result)
    }

    @Test
    fun `getOrders returns the backend error on a 4xx response`() = runTest {
        val client =
            clientResponding(errorJson(404, "Not found", "MK-404"), HttpStatusCode.NotFound)

        val result = client.getOrders()

        assertEquals(Resource.Error(MakaluError(404, "Not found", "MK-404")), result)
    }

    @Test
    fun `getOrders returns an unexpected http error when the error body is not parseable`() =
        runTest {
            val client = OrdersClient(
                mockHttpClientManager {
                    respond("Internal error", HttpStatusCode.InternalServerError)
                }
            )

            val result = client.getOrders()

            assertEquals(Resource.Error(unexpectedErrorWithHttpStatusCode(500)), result)
        }

    @Test
    fun `getOrders returns an unexpected error when the success body is malformed`() = runTest {
        val client = clientResponding("""{"not":"a list"}""")

        val result = client.getOrders()

        assertTrue(result is Resource.Error)
        val error = (result as Resource.Error).error
        assertEquals("MK-0", error.internalCode)
        assertEquals(-1, error.httpCode)
    }

    @Test
    fun `getOrder sends GET to the order id path`() = runTest {
        val client = clientResponding(orderDetailedJson())

        client.getOrder(42)

        assertEquals(HttpMethod.Get, capturedRequest?.method)
        assertEquals("/orders/42", capturedRequest?.url?.encodedPath)
    }

    @Test
    fun `getOrder decodes the detailed order on success`() = runTest {
        val client = clientResponding(orderDetailedJson())

        val result = client.getOrder(42)

        assertEquals(Resource.Success(orderDetailedResponse()), result)
    }

    @Test
    fun `getOrder decodes nullable item fields`() = runTest {
        val itemJson = """
            {
              "id": 1,
              "menu_item_id": 50,
              "image_id": null,
              "name": "Salmon roll",
              "price": "12.90",
              "notes": null,
              "configurations": []
            }
        """.trimIndent()
        val client = clientResponding(orderDetailedJson(itemJson))

        val item = client.getOrder(42).successData().items.single()

        assertNull(item.imageId)
        assertNull(item.notes)
        assertTrue(item.configurations.isEmpty())
    }

    @Test
    fun `getOrder returns the backend error on a 4xx response`() = runTest {
        val client =
            clientResponding(errorJson(404, "Order not found", "MK-404"), HttpStatusCode.NotFound)

        val result = client.getOrder(42)

        assertEquals(Resource.Error(MakaluError(404, "Order not found", "MK-404")), result)
    }
}
