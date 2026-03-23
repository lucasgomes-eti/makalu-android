package eti.lucasgomes.makalu.features.home

import eti.lucasgomes.makalu.features.home.model.CategoryResponse
import eti.lucasgomes.makalu.features.home.model.StoreResponse
import eti.lucasgomes.makalu.shared.model.AddressResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

class HomeClient(
    private val httpClientManager: HttpClientManager
) {

    suspend fun getCategories(): Resource<List<CategoryResponse>> =
        httpClientManager.withApiResource { get("/categories") }

    suspend fun getStores(): Resource<List<StoreResponse>> =
        httpClientManager.withApiResource { get("/stores") }

    suspend fun getSelfAddress(): Resource<AddressResponse> = httpClientManager.withApiResource {
        get("/addresses")
    }
}
