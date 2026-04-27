package eti.lucasgomes.features.store

import eti.lucasgomes.features.store.model.MenuItemResponse
import eti.lucasgomes.makalu.shared.model.StoreResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

internal class StoreClient(private val httpClientManager: HttpClientManager) {

    suspend fun getStore(id: Long): Resource<StoreResponse> = httpClientManager.withApiResource {
        get("/stores/$id")
    }

    suspend fun getMenuItems(storeId: Long): Resource<List<MenuItemResponse>> =
        httpClientManager.withApiResource {
            get("/stores/$storeId/menu")
        }
}