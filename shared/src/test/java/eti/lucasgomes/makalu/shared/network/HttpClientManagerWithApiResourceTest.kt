package eti.lucasgomes.makalu.shared.network

import eti.lucasgomes.makalu.testing.mockHttpClient
import eti.lucasgomes.makalu.testing.respondJson
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class HttpClientManagerWithApiResourceTest {

    @Serializable
    data class Item(val id: Long, val name: String)

    private val backendErrorJson = """
        {"http_code": 422, "message": "Invalid", "internal_code": "MK-7",
         "field_errors": [{"field": "name", "message": "Required"}]}
    """.trimIndent()

    private val backendError = MakaluError(
        httpCode = 422,
        message = "Invalid",
        internalCode = "MK-7",
        fieldErrors = listOf(MakaluError.FieldError("name", "Required"))
    )

    private fun managerRespondingWith(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): HttpClientManager {
        val client = mockHttpClient(handler)
        return mockk {
            every { getClient() } returns client
        }
    }

    @Test
    fun `returns success with the decoded body on 200`() = runTest {
        val manager = managerRespondingWith { respondJson("""{"id":1,"name":"a"}""") }

        val result = manager.withApiResource<Item> { get("items/1") }

        assertEquals(Resource.Success(Item(1, "a")), result)
    }

    @Test
    fun `returns success for any 2xx status`() = runTest {
        val manager = managerRespondingWith {
            respondJson("""{"id":2,"name":"b"}""", HttpStatusCode.Created)
        }

        val result = manager.withApiResource<Item> { get("items") }

        assertEquals(Resource.Success(Item(2, "b")), result)
    }

    @Test
    fun `returns success with unit on 204 without body`() = runTest {
        val manager = managerRespondingWith { respond("", HttpStatusCode.NoContent) }

        val result = manager.withApiResource<Unit> { get("items") }

        assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun `ignores unknown keys in the success body`() = runTest {
        val manager = managerRespondingWith {
            respondJson("""{"id":1,"name":"a","extra":true}""")
        }

        val result = manager.withApiResource<Item> { get("items/1") }

        assertEquals(Resource.Success(Item(1, "a")), result)
    }

    @Test
    fun `invokes onSuccess with the decoded body and not onFailure`() = runTest {
        val manager = managerRespondingWith { respondJson("""{"id":1,"name":"a"}""") }
        val received = mutableListOf<Item>()
        var failureCalled = false

        manager.withApiResource<Item>(
            onSuccess = { received += it },
            onFailure = { failureCalled = true }
        ) { get("items/1") }

        assertEquals(listOf(Item(1, "a")), received)
        assertEquals(false, failureCalled)
    }

    @Test
    fun `returns an unexpected error when the success body cannot be decoded`() = runTest {
        val manager = managerRespondingWith { respondJson("not json") }
        var successCalled = false

        val result = manager.withApiResource<Item>(onSuccess = { successCalled = true }) {
            get("items/1")
        }

        val error = (result as Resource.Error).error
        assertEquals(-1, error.httpCode)
        assertEquals("MK-0", error.internalCode)
        assertTrue(error.message.startsWith("Unexpected error during request."))
        assertEquals(false, successCalled)
    }

    @Test
    fun `returns an unexpected error when onSuccess throws`() = runTest {
        val manager = managerRespondingWith { respondJson("""{"id":1,"name":"a"}""") }

        val result = manager.withApiResource<Item>(
            onSuccess = { throw IllegalStateException("x") }
        ) { get("items/1") }

        assertEquals(
            Resource.Error(unexpectedErrorWithException(IllegalStateException("x"))),
            result
        )
    }

    @Test
    fun `returns the decoded backend error on 4xx and passes it to onFailure`() = runTest {
        val manager = managerRespondingWith {
            respondJson(backendErrorJson, HttpStatusCode.UnprocessableEntity)
        }
        val failures = mutableListOf<MakaluError>()
        var successCalled = false

        val result = manager.withApiResource<Item>(
            onSuccess = { successCalled = true },
            onFailure = { failures += it }
        ) { get("items") }

        assertEquals(Resource.Error(backendError), result)
        assertEquals(listOf(backendError), failures)
        assertEquals(false, successCalled)
    }

    @Test
    fun `returns the decoded backend error on 5xx`() = runTest {
        val manager = managerRespondingWith {
            respondJson(backendErrorJson, HttpStatusCode.InternalServerError)
        }

        val result = manager.withApiResource<Item> { get("items") }

        assertEquals(Resource.Error(backendError), result)
    }

    @Test
    fun `keeps the http code from the error body rather than the response status`() = runTest {
        val manager = managerRespondingWith {
            respondJson(
                """{"http_code": 409, "message": "Conflict", "internal_code": "MK-9"}""",
                HttpStatusCode.BadRequest
            )
        }

        val result = manager.withApiResource<Item> { get("items") }

        assertEquals(409, (result as Resource.Error).error.httpCode)
    }

    @Test
    fun `falls back to an unexpected http error when the error body cannot be decoded`() = runTest {
        val manager = managerRespondingWith { respond("Not Found", HttpStatusCode.NotFound) }
        val failures = mutableListOf<MakaluError>()

        val result = manager.withApiResource<Item>(onFailure = { failures += it }) {
            get("items")
        }

        val expected = unexpectedErrorWithHttpStatusCode(404)
        assertEquals(Resource.Error(expected), result)
        assertEquals(listOf(expected), failures)
    }

    @Test
    fun `returns an unexpected http error for statuses outside 2xx and 4xx-5xx`() = runTest {
        val manager = managerRespondingWith { respond("", HttpStatusCode.NotModified) }
        val failures = mutableListOf<MakaluError>()

        val result = manager.withApiResource<Item>(onFailure = { failures += it }) {
            get("items")
        }

        val expected = unexpectedErrorWithHttpStatusCode(304)
        assertEquals(Resource.Error(expected), result)
        assertEquals(listOf(expected), failures)
    }

    @Test
    fun `returns an unexpected error when the request throws`() = runTest {
        val manager = managerRespondingWith { throw IOException("offline") }

        val result = manager.withApiResource<Item> { get("items") }

        val error = (result as Resource.Error).error
        assertEquals(-1, error.httpCode)
        assertEquals("MK-0", error.internalCode)
        assertTrue(error.message.contains("offline"))
    }

    @Test
    fun `rethrows cancellation from the request`() = runTest {
        val manager = managerRespondingWith { respondJson("{}") }

        try {
            manager.withApiResource<Item> { throw CancellationException("cancelled") }
            fail("expected CancellationException")
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
    }
}
