package eti.lucasgomes.makalu.features.profile

import android.content.ContentResolver
import android.net.Uri
import eti.lucasgomes.makalu.features.profile.model.ProfileResponse
import eti.lucasgomes.makalu.shared.model.ImageUploadResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class ProfileClient(
    private val httpClientManager: HttpClientManager,
    private val contentResolver: ContentResolver
) {

    suspend fun getSelfProfile(): Resource<ProfileResponse> = httpClientManager.withApiResource {
        get("profile")
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