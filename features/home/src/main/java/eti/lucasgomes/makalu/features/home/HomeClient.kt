package eti.lucasgomes.makalu.features.home

import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

class HomeClient(
    private val httpClientManager: HttpClientManager
) {

    suspend fun getCategories(): Resource<List<CategoryResponse>> =
        httpClientManager.withApiResource {
            get("/categories")
        }
}
