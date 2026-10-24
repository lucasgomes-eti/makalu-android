package eti.lucasgomes.makalu.features.profile

import android.content.ContentResolver
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.shared.model.ImageUploadResponse
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.mockHttpClientManager
import eti.lucasgomes.makalu.testing.respondJson
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

@RunWith(AndroidJUnit4::class)
class ProfileClientTest {

    private val contentResolver: ContentResolver = mockk()
    private val imageUri = Uri.parse("content://media/external/images/photo.jpg")
    private val imageBytes = "fake-jpeg-bytes".toByteArray()

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = PROFILE_JSON,
        onRequest: suspend (HttpRequestData) -> Unit = {}
    ) = ProfileClient(
        httpClientManager = mockHttpClientManager { request ->
            onRequest(request)
            respondJson(body, status)
        },
        contentResolver = contentResolver
    )

    private fun givenImageCanBeRead() {
        every { contentResolver.openInputStream(imageUri) } returns ByteArrayInputStream(imageBytes)
    }

    @Test
    fun `get self profile sends GET to profile path`() = runTest {
        var request: HttpRequestData? = null

        client(onRequest = { request = it }).getSelfProfile()

        assertEquals(HttpMethod.Get, request?.method)
        assertEquals("/profile", request?.url?.encodedPath)
    }

    @Test
    fun `get self profile decodes snake case response`() = runTest {
        val result = client().getSelfProfile()

        assertEquals(
            Resource.Success(
                profileResponse(
                    id = 1,
                    name = "Ana Silva",
                    email = "ana@makalu.com",
                    phoneNumber = "11999999999",
                    profileImageId = 5
                )
            ),
            result
        )
    }

    @Test
    fun `get self profile decodes null profile image id`() = runTest {
        val body = """
            {"id":1,"name":"Ana","email":"ana@makalu.com","phone_number":"119","profile_image_id":null}
        """

        val result = client(body = body).getSelfProfile()

        assertNull((result as Resource.Success).data.profileImageId)
    }

    @Test
    fun `get self profile maps api error body to resource error`() = runTest {
        val body = makaluErrorJson(404, "Profile not found", "MK-404")

        val result = client(HttpStatusCode.NotFound, body).getSelfProfile()

        assertEquals(Resource.Error(makaluError(404, "Profile not found", "MK-404")), result)
    }

    @Test
    fun `get self profile with missing required field returns unexpected error`() = runTest {
        val body = """{"id":1,"name":"Ana","email":"ana@makalu.com","profile_image_id":5}"""

        val result = client(body = body).getSelfProfile()

        val error = (result as Resource.Error).error
        assertEquals("MK-0", error.internalCode)
        assertEquals(-1, error.httpCode)
    }

    @Test
    fun `upload image posts multipart form to upload image path`() = runTest {
        givenImageCanBeRead()
        var request: HttpRequestData? = null

        client(body = """{"id":99}""", onRequest = { request = it }).uploadImage(imageUri)

        assertEquals(HttpMethod.Post, request?.method)
        assertEquals("/profile/upload-image", request?.url?.encodedPath)
        assertTrue(request?.body?.contentType?.match(ContentType.MultiPart.FormData) == true)
    }

    @Test
    fun `upload image sends file part with jpeg content type filename and bytes`() = runTest {
        givenImageCanBeRead()
        var sentBody = ""

        client(body = """{"id":99}""", onRequest = {
            sentBody = it.body.toByteArray().decodeToString()
        }).uploadImage(imageUri)

        assertTrue(sentBody, sentBody.contains("Content-Disposition: form-data; name=\"file\"; filename=photo.jpg"))
        assertTrue(sentBody, sentBody.contains("Content-Type: image/jpeg"))
        assertTrue(sentBody, sentBody.contains("fake-jpeg-bytes"))
    }

    @Test
    fun `upload image decodes the uploaded image id`() = runTest {
        givenImageCanBeRead()

        val result = client(body = """{"id":99}""").uploadImage(imageUri)

        assertEquals(Resource.Success(ImageUploadResponse(99)), result)
    }

    @Test
    fun `upload image maps api error body to resource error`() = runTest {
        givenImageCanBeRead()
        val body = makaluErrorJson(413, "Image too large", "MK-413")

        val result = client(HttpStatusCode.PayloadTooLarge, body).uploadImage(imageUri)

        assertEquals(Resource.Error(makaluError(413, "Image too large", "MK-413")), result)
    }

    @Test
    fun `upload image returns unexpected error without sending a request when the file cannot be opened`() =
        runTest {
            every { contentResolver.openInputStream(imageUri) } returns null
            var requestCount = 0

            val result = client(onRequest = { requestCount++ }).uploadImage(imageUri)

            val error = (result as Resource.Error).error
            assertEquals("MK-0", error.internalCode)
            assertTrue(error.message.contains("Error while reading file from memory."))
            assertEquals(0, requestCount)
        }

    @Test
    fun `upload image returns unexpected error when the resolver throws`() = runTest {
        every { contentResolver.openInputStream(imageUri) } throws FileNotFoundException("gone")

        val result = client().uploadImage(imageUri)

        val error = (result as Resource.Error).error
        assertEquals("MK-0", error.internalCode)
        assertTrue(error.message.contains("gone"))
    }
}
