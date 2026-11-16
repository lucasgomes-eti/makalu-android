package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.features.orders.model.OrderDetailedResponse
import eti.lucasgomes.makalu.features.orders.model.RealtimeCredentialsResponse
import eti.lucasgomes.makalu.features.orders.model.UpdateOrderStatusRequest
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.Resource
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody

internal class OrdersClient(private val httpClientManager: HttpClientManager) {

    suspend fun getOrders(): Resource<List<OrderSimpleResponse>> =
        httpClientManager.withApiResource {
            get("/orders")
        }

    suspend fun getOrder(id: Long): Resource<OrderDetailedResponse> =
        httpClientManager.withApiResource {
            get("/orders/$id")
        }

    suspend fun getRealtimeCredentials(orderId: Long): Resource<RealtimeCredentialsResponse> =
        httpClientManager.withApiResource {
            get("/orders/$orderId/realtime")
        }

    suspend fun finishOrder(orderId: Long): Resource<OrderDetailedResponse> =
        httpClientManager.withApiResource {
            patch("/orders/$orderId/status") {
                setBody(UpdateOrderStatusRequest(OrderStatus.FINISHED))
            }
        }
}
