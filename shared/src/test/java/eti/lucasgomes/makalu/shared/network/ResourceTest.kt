package eti.lucasgomes.makalu.shared.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class ResourceTest {

    private val error = MakaluError(httpCode = 400, message = "Invalid", internalCode = "MK-1")

    @Test
    fun `map transforms success data`() {
        val resource: Resource<Int> = Resource.Success(2)

        val result = resource.map { it * 10 }

        assertEquals(Resource.Success(20), result)
    }

    @Test
    fun `map keeps the error and skips the transform on error`() {
        val resource: Resource<Int> = Resource.Error(error)

        val result = resource.map<Int, Int> { fail("transform should not run"); it }

        assertEquals(Resource.Error(error), result)
    }

    @Test
    fun `asEmptyDataResult turns success into success with unit`() {
        val resource: Resource<String> = Resource.Success("x")

        val result = resource.asEmptyDataResult()

        assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun `asEmptyDataResult keeps the error`() {
        val resource: Resource<String> = Resource.Error(error)

        val result = resource.asEmptyDataResult()

        assertEquals(Resource.Error(error), result)
    }

    @Test
    fun `onSuccess runs the action with the data and returns the same resource`() {
        val resource: Resource<String> = Resource.Success("x")
        var received: String? = null

        val result = resource.onSuccess { received = it }

        assertEquals("x", received)
        assertSame(resource, result)
    }

    @Test
    fun `onSuccess does not run the action on error`() {
        val resource: Resource<String> = Resource.Error(error)
        var called = false

        val result = resource.onSuccess { called = true }

        assertFalse(called)
        assertSame(resource, result)
    }

    @Test
    fun `onError runs the action with the error and returns the same resource`() {
        val resource: Resource<String> = Resource.Error(error)
        var received: MakaluError? = null

        val result = resource.onError { received = it }

        assertEquals(error, received)
        assertSame(resource, result)
    }

    @Test
    fun `onError does not run the action on success`() {
        val resource: Resource<String> = Resource.Success("x")
        var called = false

        val result = resource.onError { called = true }

        assertFalse(called)
        assertSame(resource, result)
    }

    @Test
    fun `chained onSuccess and onError only run the matching branch`() {
        val calls = mutableListOf<String>()

        (Resource.Success("x") as Resource<String>)
            .onSuccess { calls += "success" }
            .onError { calls += "error" }
        (Resource.Error(error) as Resource<String>)
            .onSuccess { calls += "success" }
            .onError { calls += "error" }

        assertEquals(listOf("success", "error"), calls)
    }

    @Test
    fun `unexpectedErrorWithException builds an MK-0 error with code -1 and the cause message`() {
        val result = unexpectedErrorWithException(IOException("boom"))

        assertEquals(
            MakaluError(
                httpCode = -1,
                message = "Unexpected error during request. Cause: boom",
                internalCode = "MK-0",
                fieldErrors = emptyList()
            ),
            result
        )
    }

    @Test
    fun `unexpectedErrorWithException includes null when the exception has no message`() {
        val result = unexpectedErrorWithException(IllegalStateException())

        assertTrue(result.message.endsWith("Cause: null"))
    }

    @Test
    fun `unexpectedErrorWithHttpStatusCode builds an MK-0 error with the status code`() {
        val result = unexpectedErrorWithHttpStatusCode(503)

        assertEquals(
            MakaluError(
                httpCode = 503,
                message = "Unexpected http error. Code: 503",
                internalCode = "MK-0"
            ),
            result
        )
    }
}
