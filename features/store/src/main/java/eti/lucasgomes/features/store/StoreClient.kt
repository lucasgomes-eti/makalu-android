package eti.lucasgomes.features.store

import eti.lucasgomes.makalu.shared.model.StoreResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

internal class StoreClient(private val httpClientManager: HttpClientManager) {

    suspend fun getStore(id: Long): Resource<StoreResponse> = httpClientManager.withApiResource {
        get("/stores/$id")
    }
}