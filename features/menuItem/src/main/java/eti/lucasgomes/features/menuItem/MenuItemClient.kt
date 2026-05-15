package eti.lucasgomes.features.menuItem

import eti.lucasgomes.features.menuItem.model.CartItemRequest
import eti.lucasgomes.features.menuItem.model.MenuItemResponse
import eti.lucasgomes.makalu.shared.network.EmptyResult
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody

internal class MenuItemClient(private val httpClientManager: HttpClientManager) {

    suspend fun getItem(id: Long): Resource<MenuItemResponse> = httpClientManager.withApiResource {
        get("/stores/menu/$id")
    }

    suspend fun addItemToCart(
        storeId: Long,
        menuItemId: Long,
        request: CartItemRequest
    ): EmptyResult =
        httpClientManager.withApiResource {
            put("/stores/$storeId/menu/$menuItemId/cart") {
                setBody(request)
            }
        }
}