package eti.lucasgomes.makalu.features.auth

import android.content.ContentResolver
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.features.auth.login.model.LoginRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterResponse
import eti.lucasgomes.makalu.shared.model.ImageUploadResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.network.TokenPairResponse
import eti.lucasgomes.makalu.shared.network.unexpectedErrorWithHttpStatusCode
import eti.lucasgomes.makalu.shared.settings.Settings
import eti.lucasgomes.makalu.testing.mockHttpClientManager
import eti.lucasgomes.makalu.testing.respondJson
import eti.lucasgomes.makalu.testing.testJson
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream

@RunWith(AndroidJUnit4::class)
class AuthClientTest {

    private val initialSettings = Settings(
        accessToken = "old-access",
        refreshToken = "old-refresh",
        addressId = 3,
        addressName = "Home"
    )
    private val dataStore = FakeSettingsDataStore(initialSettings)
    private val contentResolver: ContentResolver = mockk()
    private val imageUri = Uri.parse("content://media/images/pic.jpg")
    private val imageBytes = "fake-jpeg-bytes".toByteArray()
    private val registerRequest = RegisterRequest(
        name = "Ana Silva",
        email = "ana@makalu.com",
        phoneNumber = "11999999999",
        password = "secret123"
    )
    private val loginRequest = LoginRequest(email = "ana@makalu.com", password = "secret123")

    private lateinit var httpClientManager: HttpClientManager
    private val requests = mutableListOf<HttpRequestData>()

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}"
    ): AuthClient {
        httpClientManager = mockHttpClientManager { request ->
            requests += request
            respondJson(body, status)
        }
        return AuthClient(httpClientManager, dataStore, contentResolver)
    }

    private fun HttpRequestData.jsonBody(): JsonObject =
        testJson.parseToJsonElement((body as TextContent).text).jsonObject

    private fun givenImageCanBeRead() {
        every { contentResolver.openInputStream(imageUri) } returns ByteArrayInputStream(imageBytes)
    }

    @Test
    fun `register sends POST to auth register path`() = runTest {
        client(body = registerResponseJson(7, "Ana Silva", "ana@makalu.com", "11999999999"))
            .register(registerRequest)

        assertEquals(HttpMethod.Post, requests.single().method)
        assertEquals("/auth/register", requests.single().url.encodedPath)
    }

    @Test
    fun `register serializes the request with snake case phone number`() = runTest {
        client(body = registerResponseJson(7, "Ana Silva", "ana@makalu.com", "11999999999"))
            .register(registerRequest)

        val body = requests.single().jsonBody()
        assertEquals("Ana Silva", body.getValue("name").jsonPrimitive.content)
        assertEquals("ana@makalu.com", body.getValue("email").jsonPrimitive.content)
        assertEquals("11999999999", body.getValue("phone_number").jsonPrimitive.content)
        assertEquals("secret123", body.getValue("password").jsonPrimitive.content)
        assertEquals(4, body.size)
    }

    @Test
    fun `register decodes the created user on success`() = runTest {
        val result = client(body = registerResponseJson(7, "Ana Silva", "ana@makalu.com", "11999999999"))
            .register(registerRequest)

        assertEquals(
            Resource.Success(RegisterResponse(7, "Ana Silva", "ana@makalu.com", "11999999999")),
            result
        )
    }

    @Test
    fun `register returns the backend error with field errors on a 4xx response`() = runTest {
        val fieldErrors = listOf(MakaluError.FieldError("email", "Email already in use"))
        val body = makaluErrorJson(422, "Validation failed", "MK-422", fieldErrors)

        val result = client(HttpStatusCode.UnprocessableEntity, body).register(registerRequest)

        assertEquals(Resource.Error(makaluError(422, "Validation failed", "MK-422", fieldErrors)), result)
    }

    @Test
    fun `register returns an unexpected http error when the error body is not parseable`() = runTest {
        val result = client(HttpStatusCode.InternalServerError, "oops").register(registerRequest)

        assertEquals(Resource.Error(unexpectedErrorWithHttpStatusCode(500)), result)
    }

    @Test
    fun `login sends POST to auth login path with credentials`() = runTest {
        client(body = tokenPairJson("new-access", "new-refresh")).login(loginRequest)

        val request = requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/auth/login", request.url.encodedPath)
        assertEquals("ana@makalu.com", request.jsonBody().getValue("email").jsonPrimitive.content)
        assertEquals("secret123", request.jsonBody().getValue("password").jsonPrimitive.content)
    }

    @Test
    fun `login decodes the token pair on success`() = runTest {
        val result = client(body = tokenPairJson("new-access", "new-refresh")).login(loginRequest)

        assertEquals(Resource.Success(TokenPairResponse("new-access", "new-refresh")), result)
    }

    @Test
    fun `login stores both tokens in settings on success`() = runTest {
        client(body = tokenPairJson("new-access", "new-refresh")).login(loginRequest)

        assertEquals(
            initialSettings.copy(accessToken = "new-access", refreshToken = "new-refresh"),
            dataStore.current
        )
    }

    @Test
    fun `login reinstalls auth on the http client on success`() = runTest {
        client(body = tokenPairJson("new-access", "new-refresh")).login(loginRequest)

        verify(exactly = 1) { httpClientManager.installAuth() }
    }

    @Test
    fun `login failure leaves settings untouched and does not reinstall auth`() = runTest {
        val body = makaluErrorJson(401, "Invalid credentials", "MK-401")

        val result = client(HttpStatusCode.Unauthorized, body).login(loginRequest)

        assertEquals(Resource.Error(makaluError(401, "Invalid credentials", "MK-401")), result)
        assertEquals(initialSettings, dataStore.current)
        verify(exactly = 0) { httpClientManager.installAuth() }
    }

    @Test
    fun `uploadImage posts multipart form to profile upload image path`() = runTest {
        givenImageCanBeRead()

        client(body = """{"id":99}""").uploadImage(imageUri)

        val request = requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/profile/upload-image", request.url.encodedPath)
        assertTrue(request.body.contentType?.match(ContentType.MultiPart.FormData) == true)
    }

    @Test
    fun `uploadImage sends the file part as jpeg with the uri file name`() = runTest {
        givenImageCanBeRead()

        client(body = """{"id":99}""").uploadImage(imageUri)

        val sentBody = requests.single().body.toByteArray().decodeToString()
        assertTrue(sentBody, sentBody.contains("Content-Disposition: form-data; name=\"file\"; filename=pic.jpg"))
        assertTrue(sentBody, sentBody.contains("Content-Type: image/jpeg"))
        assertTrue(sentBody, sentBody.contains("fake-jpeg-bytes"))
    }

    @Test
    fun `uploadImage decodes the uploaded image id on success`() = runTest {
        givenImageCanBeRead()

        val result = client(body = """{"id":99}""").uploadImage(imageUri)

        assertEquals(Resource.Success(ImageUploadResponse(99)), result)
    }

    @Test
    fun `uploadImage returns an unexpected error without sending a request when the file cannot be opened`() = runTest {
        every { contentResolver.openInputStream(imageUri) } returns null

        val result = client(body = """{"id":99}""").uploadImage(imageUri)

        val error = (result as Resource.Error).error
        assertEquals(-1, error.httpCode)
        assertEquals("MK-0", error.internalCode)
        assertTrue(error.message, error.message.contains("Error while reading file from memory."))
        assertTrue(requests.isEmpty())
    }

    @Test
    fun `uploadImage returns the backend error on a 4xx response`() = runTest {
        givenImageCanBeRead()
        val body = makaluErrorJson(413, "Image too large", "MK-413")

        val result = client(HttpStatusCode.PayloadTooLarge, body).uploadImage(imageUri)

        assertEquals(Resource.Error(makaluError(413, "Image too large", "MK-413")), result)
    }
}
