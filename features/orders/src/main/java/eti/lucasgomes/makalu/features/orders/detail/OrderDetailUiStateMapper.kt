package eti.lucasgomes.makalu.features.orders.detail

import eti.lucasgomes.makalu.features.orders.components.OrderCardUiState
import eti.lucasgomes.makalu.features.orders.components.toUiStatus
import eti.lucasgomes.makalu.features.orders.model.OrderDetailedResponse
import eti.lucasgomes.makalu.shared.mapImageUrl

internal fun OrderDetailedResponse.toCardUiState(): OrderCardUiState = OrderCardUiState(
    orderNumber = orderNumber,
    status = status.toUiStatus(),
    storeImageUrl = mapImageUrl(storeImageId),
    storeName = storeName,
    deliveryAddressLine = deliveryAddressLine,
    itemsCount = items.size,
    deliveryFee = total.deliveryFee,
    totalPrice = total.total
)

internal fun OrderDetailedResponse.toUiItems(): List<OrderDetailUiState.Item> =
    items.map { item ->
        OrderDetailUiState.Item(
            id = item.id,
            name = item.name,
            imageUrl = mapImageUrl(item.imageId),
            price = item.price,
            notes = item.notes,
            configurations = item.configurations.map { configuration ->
                OrderDetailUiState.Item.Configuration(
                    id = configuration.id,
                    name = configuration.name,
                    options = configuration.options.map { option ->
                        OrderDetailUiState.Item.Configuration.Option(
                            id = option.id,
                            name = option.name,
                            additionalPrice = option.additionalPrice,
                            quantity = option.quantity
                        )
                    }
                )
            }
        )
    }
