package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

internal class OrdersClient(private val httpClientManager: HttpClientManager) {

    suspend fun getOrders(): Resource<List<OrderSimpleResponse>> =
        httpClientManager.withApiResource {
            get("/orders")
        }
}
