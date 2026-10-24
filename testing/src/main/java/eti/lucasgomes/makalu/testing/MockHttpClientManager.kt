package eti.lucasgomes.makalu.testing

import eti.lucasgomes.makalu.shared.network.HttpClientManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json

const val TEST_BASE_URL = "https://api.makalu.test/"

val testJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
}

fun mockHttpClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
): HttpClient = HttpClient(MockEngine(handler)) {
    expectSuccess = false
    defaultRequest {
        url(TEST_BASE_URL)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }
    install(ContentNegotiation) {
        json(testJson)
    }
}

fun mockHttpClientManager(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
): HttpClientManager = mockk(relaxed = true) {
    every { getClient() } returns mockHttpClient(handler)
}

fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK
): HttpResponseData = respond(
    content = body,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
)
