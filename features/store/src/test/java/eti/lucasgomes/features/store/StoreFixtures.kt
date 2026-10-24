package eti.lucasgomes.features.store

import eti.lucasgomes.features.store.model.MenuItemResponse
import eti.lucasgomes.makalu.shared.model.StoreResponse
import eti.lucasgomes.makalu.shared.network.MakaluError
import java.math.BigDecimal

internal fun storeResponse(
    id: Long = 3,
    name: String = "Pizzeria",
    coverImageId: Long? = 9,
    logoImageId: Long? = 8,
    deliveryFee: BigDecimal = BigDecimal("5.90")
) = StoreResponse(
    id = id,
    name = name,
    categories = emptyList(),
    logoImageId = logoImageId,
    coverImageId = coverImageId,
    deliveryFee = deliveryFee
)

internal fun menuItemResponse(
    id: Long = 1,
    storeId: Long = 3,
    category: String = "Pizza",
    name: String = "Margherita",
    price: BigDecimal = BigDecimal("12.50"),
    ingredients: String? = null,
    configurations: List<MenuItemResponse.Configuration> = emptyList(),
    imageId: Long? = null
) = MenuItemResponse(
    id = id,
    storeId = storeId,
    category = category,
    name = name,
    price = price,
    ingredients = ingredients,
    configurations = configurations,
    imageId = imageId
)

internal fun makaluError(
    httpCode: Int = 404,
    message: String = "Store not found",
    internalCode: String = "MK-404"
) = MakaluError(httpCode = httpCode, message = message, internalCode = internalCode)
