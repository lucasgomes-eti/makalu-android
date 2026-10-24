package eti.lucasgomes.features.cart

import eti.lucasgomes.features.cart.model.CartResponse
import eti.lucasgomes.makalu.shared.network.MakaluError
import java.math.BigDecimal

const val STORE_ID = 5L

val testError = MakaluError(httpCode = 400, message = "boom", internalCode = "MK-1")

fun cartItemResponse(
    id: Long = 1L,
    imageId: Long? = 7L,
    name: String = "Pizza",
    price: BigDecimal = BigDecimal("10.50"),
    notes: String? = "No onions",
    configurations: List<CartResponse.CartItemResponse.Configuration> = emptyList()
) = CartResponse.CartItemResponse(
    id = id,
    imageId = imageId,
    name = name,
    price = price,
    notes = notes,
    configurations = configurations
)

fun cartResponse(
    id: Long = 42L,
    deliveryAddressLine: String? = "Rua A, 10",
    items: List<CartResponse.CartItemResponse> = listOf(cartItemResponse()),
    deliveryFee: BigDecimal = BigDecimal("5.00"),
    total: BigDecimal = BigDecimal("15.50")
) = CartResponse(
    id = id,
    deliveryAddressLine = deliveryAddressLine,
    items = items,
    total = CartResponse.Total(deliveryFee = deliveryFee, total = total)
)
