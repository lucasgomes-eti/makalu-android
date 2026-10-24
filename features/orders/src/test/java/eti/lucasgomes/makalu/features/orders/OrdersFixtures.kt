package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.features.orders.model.OrderDetailedResponse
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import java.math.BigDecimal
import kotlin.time.Instant

internal val CREATED_AT = Instant.parse("2026-01-10T12:00:00Z")
internal val UPDATED_AT = Instant.parse("2026-01-10T12:30:00Z")

internal fun orderSimpleResponse(
    id: Long = 1,
    status: OrderStatus = OrderStatus.PENDING,
    orderNumber: String = "0001",
    storeName: String = "Sushi Naka",
    storeImageId: Long? = 7,
    deliveryAddressLine: String = "Rua M, 10",
    itemsCount: Int = 2,
    deliveryFee: BigDecimal = BigDecimal("2.50"),
    total: BigDecimal = BigDecimal("24.00"),
    createdAt: Instant = CREATED_AT,
    updatedAt: Instant = UPDATED_AT
) = OrderSimpleResponse(
    id = id,
    status = status,
    orderNumber = orderNumber,
    storeName = storeName,
    storeImageId = storeImageId,
    deliveryAddressLine = deliveryAddressLine,
    itemsCount = itemsCount,
    total = OrderSimpleResponse.Total(deliveryFee = deliveryFee, total = total),
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun option(
    id: Long = 100,
    name: String = "Extra cheese",
    additionalPrice: BigDecimal = BigDecimal("1.50"),
    quantity: Int = 1
) = OrderDetailedResponse.OrderItemResponse.Configuration.Option(id, name, additionalPrice, quantity)

internal fun configuration(
    id: Long = 10,
    name: String = "Extras",
    options: List<OrderDetailedResponse.OrderItemResponse.Configuration.Option> = listOf(option())
) = OrderDetailedResponse.OrderItemResponse.Configuration(id, name, options)

internal fun orderItemResponse(
    id: Long = 1,
    menuItemId: Long = 50,
    imageId: Long? = 9,
    name: String = "Salmon roll",
    price: BigDecimal = BigDecimal("12.90"),
    notes: String? = "No wasabi",
    configurations: List<OrderDetailedResponse.OrderItemResponse.Configuration> = listOf(configuration())
) = OrderDetailedResponse.OrderItemResponse(id, menuItemId, imageId, name, price, notes, configurations)

internal fun orderDetailedResponse(
    id: Long = 42,
    status: OrderStatus = OrderStatus.ACCEPTED,
    orderNumber: String = "0042",
    storeName: String = "Sushi Naka",
    storeImageId: Long? = 5,
    deliveryAddressLine: String = "Rua M, 10",
    items: List<OrderDetailedResponse.OrderItemResponse> = listOf(orderItemResponse()),
    deliveryFee: BigDecimal = BigDecimal("2.50"),
    total: BigDecimal = BigDecimal("15.40"),
    createdAt: Instant = CREATED_AT,
    updatedAt: Instant = UPDATED_AT
) = OrderDetailedResponse(
    id = id,
    status = status,
    orderNumber = orderNumber,
    storeName = storeName,
    storeImageId = storeImageId,
    deliveryAddressLine = deliveryAddressLine,
    items = items,
    total = OrderDetailedResponse.Total(deliveryFee = deliveryFee, total = total),
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun orderSimpleJson(
    id: Long = 1,
    status: String = "PENDING",
    storeImageId: String = "7",
    deliveryFee: String = "2.50",
    total: String = "24.00"
) = """
    {
      "id": $id,
      "status": "$status",
      "order_number": "0001",
      "store_name": "Sushi Naka",
      "store_image_id": $storeImageId,
      "delivery_address_line": "Rua M, 10",
      "items_count": 2,
      "total": { "delivery_fee": "$deliveryFee", "total": "$total" },
      "created_at": "2026-01-10T12:00:00Z",
      "updated_at": "2026-01-10T12:30:00Z"
    }
""".trimIndent()

internal fun orderDetailedJson(itemsJson: String = orderItemJson()) = """
    {
      "id": 42,
      "status": "ACCEPTED",
      "order_number": "0042",
      "store_name": "Sushi Naka",
      "store_image_id": 5,
      "delivery_address_line": "Rua M, 10",
      "items": [$itemsJson],
      "total": { "delivery_fee": "2.50", "total": "15.40" },
      "created_at": "2026-01-10T12:00:00Z",
      "updated_at": "2026-01-10T12:30:00Z"
    }
""".trimIndent()

internal fun orderItemJson() = """
    {
      "id": 1,
      "menu_item_id": 50,
      "image_id": 9,
      "name": "Salmon roll",
      "price": "12.90",
      "notes": "No wasabi",
      "configurations": [
        {
          "id": 10,
          "name": "Extras",
          "options": [
            { "id": 100, "name": "Extra cheese", "additional_price": "1.50", "quantity": 1 }
          ]
        }
      ]
    }
""".trimIndent()

internal fun errorJson(httpCode: Int, message: String, internalCode: String) = """
    { "http_code": $httpCode, "message": "$message", "internal_code": "$internalCode" }
""".trimIndent()
