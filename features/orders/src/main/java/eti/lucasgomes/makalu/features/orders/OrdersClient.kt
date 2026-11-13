package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.features.orders.model.OrderDetailedResponse
import eti.lucasgomes.makalu.features.orders.model.RealtimeCredentialsResponse
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get

internal class OrdersClient(private val httpClientManager: HttpClientManager) {

    suspend fun getOrders(): Resource<List<OrderSimpleResponse>> =
        httpClientManager.withApiResource {
            get("/orders")
        }

    suspend fun getOrder(id: Long): Resource<OrderDetailedResponse> =
        httpClientManager.withApiResource {
            get("/orders/$id")
        }

    /**
     * A read-only key for the order's realtime channel. The API answers 503 (`MK-801`)
     * while realtime is switched off.
     */
    suspend fun getRealtimeCredentials(orderId: Long): Resource<RealtimeCredentialsResponse> =
        httpClientManager.withApiResource {
            get("/orders/$orderId/realtime")
        }
}
