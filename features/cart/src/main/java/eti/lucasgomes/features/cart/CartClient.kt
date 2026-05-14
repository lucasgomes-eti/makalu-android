package eti.lucasgomes.features.cart

import eti.lucasgomes.features.cart.model.CartResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

class CartClient(private val httpClientManager: HttpClientManager) {

    suspend fun getByStore(storeId: Long): Resource<CartResponse> =
        httpClientManager.withApiResource {
            get("/stores/$storeId/cart")
        }
}