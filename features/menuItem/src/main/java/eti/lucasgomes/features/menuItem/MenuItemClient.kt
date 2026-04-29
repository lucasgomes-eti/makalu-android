package eti.lucasgomes.features.menuItem

import eti.lucasgomes.features.menuItem.model.MenuItemResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

internal class MenuItemClient(private val httpClientManager: HttpClientManager) {

    suspend fun getItem(id: Long): Resource<MenuItemResponse> = httpClientManager.withApiResource {
        get("/stores/menu/$id")
    }
}