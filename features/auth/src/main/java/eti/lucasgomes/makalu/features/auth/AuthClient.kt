package eti.lucasgomes.makalu.features.auth

import android.content.ContentResolver
import android.net.Uri
import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.features.auth.login.model.LoginRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterResponse
import eti.lucasgomes.makalu.shared.model.ImageUploadResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.network.TokenPairResponse
import eti.lucasgomes.makalu.shared.settings.Settings
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class AuthClient(
    private val httpClientManager: HttpClientManager,
    private val dataStore: DataStore<Settings>,
    private val contentResolver: ContentResolver
) {

    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> =
        httpClientManager.withApiResource {
            post("auth/register") { setBody(request) }
        }

    suspend fun login(request: LoginRequest): Resource<TokenPairResponse> =
        httpClientManager.withApiResource(onSuccess = { response ->
            dataStore.updateData { settings ->
                settings.copy(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
            }
            httpClientManager.installAuth()
        }) {
            post("auth/login") { setBody(request) }
        }

    suspend fun uploadImage(uri: Uri): Resource<ImageUploadResponse> =
        httpClientManager.withApiResource {
            submitFormWithBinaryData(url = "profile/upload-image", formData = formData {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    append("file", inputStream.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=${uri.lastPathSegment}")
                    })
                } ?: throw Exception("Error while reading file from memory.")
            })
        }
}