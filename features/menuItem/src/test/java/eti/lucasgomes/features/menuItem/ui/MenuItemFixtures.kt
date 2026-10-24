package eti.lucasgomes.features.menuItem.ui

import eti.lucasgomes.features.menuItem.model.MenuItemResponse
import eti.lucasgomes.features.menuItem.ui.model.ConfigurationUiState
import eti.lucasgomes.makalu.shared.network.MakaluError
import java.math.BigDecimal

internal const val STORE_ID = 7L
internal const val MENU_ITEM_ID = 42L

internal val singleKey = ConfigurationUiState("Size", ConfigurationUiState.Type.SINGLE_CHOICE)
internal val multipleKey = ConfigurationUiState("Extras", ConfigurationUiState.Type.MULTIPLE_CHOICE)
internal val quantityKey = ConfigurationUiState("Drinks", ConfigurationUiState.Type.QUANTITY)

internal fun responseOption(id: Long, price: String = "1.00") =
    MenuItemResponse.Configuration.Option(id, "Option $id", BigDecimal(price))

internal fun singleChoiceConfiguration(
    vararg options: MenuItemResponse.Configuration.Option = arrayOf(
        responseOption(10), responseOption(11), responseOption(12)
    )
) = MenuItemResponse.Configuration(1, "Size", MenuItemResponse.Configuration.Type.SINGLE_CHOICE, options.toList())

internal fun multipleChoiceConfiguration(
    vararg options: MenuItemResponse.Configuration.Option = arrayOf(
        responseOption(20), responseOption(21), responseOption(22)
    )
) = MenuItemResponse.Configuration(2, "Extras", MenuItemResponse.Configuration.Type.MULTIPLE_CHOICE, options.toList())

internal fun quantityConfiguration(
    vararg options: MenuItemResponse.Configuration.Option = arrayOf(
        responseOption(30), responseOption(31)
    )
) = MenuItemResponse.Configuration(3, "Drinks", MenuItemResponse.Configuration.Type.QUANTITY, options.toList())

internal fun menuItemResponse(
    name: String = "Margherita",
    price: BigDecimal = BigDecimal("12.50"),
    ingredients: String? = "Tomato, mozzarella",
    imageId: Long? = 9,
    configurations: List<MenuItemResponse.Configuration> = listOf(
        singleChoiceConfiguration(),
        multipleChoiceConfiguration(),
        quantityConfiguration()
    )
) = MenuItemResponse(
    id = MENU_ITEM_ID,
    storeId = STORE_ID,
    category = "Pizza",
    name = name,
    price = price,
    ingredients = ingredients,
    configurations = configurations,
    imageId = imageId
)

internal fun makaluError(internalCode: String = "MK-1", message: String = "Not found") =
    MakaluError(httpCode = 404, message = message, internalCode = internalCode)
