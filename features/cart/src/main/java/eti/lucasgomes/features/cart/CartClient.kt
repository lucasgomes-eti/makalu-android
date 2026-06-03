package eti.lucasgomes.features.cart

import eti.lucasgomes.features.cart.model.CartResponse
import eti.lucasgomes.features.cart.model.CreateOrderRequest
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.network.EmptyResult
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class CartClient(private val httpClientManager: HttpClientManager) {

    suspend fun getByStore(storeId: Long): Resource<CartResponse> =
        httpClientManager.withApiResource {
            get("/stores/$storeId/cart")
        }

    suspend fun deleteByItem(storeId: Long, cartItemId: Long): Resource<CartResponse> =
        httpClientManager.withApiResource {
            delete("/stores/$storeId/cart/items/$cartItemId")
        }

    suspend fun deleteAllItemsByStore(storeId: Long): EmptyResult =
        httpClientManager.withApiResource {
            delete("/stores/$storeId/cart/items")
        }

    suspend fun submitOrder(request: CreateOrderRequest): Resource<OrderSimpleResponse> =
        httpClientManager.withApiResource {
            post("/orders") { setBody(request) }
        }
}
