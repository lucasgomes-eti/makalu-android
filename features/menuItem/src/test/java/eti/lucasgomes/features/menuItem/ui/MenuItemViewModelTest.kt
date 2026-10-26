package eti.lucasgomes.features.menuItem.ui

import MakaluConfig
import eti.lucasgomes.features.menuItem.MenuItemClient
import eti.lucasgomes.features.menuItem.model.CartItemRequest
import eti.lucasgomes.features.menuItem.model.MenuItemResponse
import eti.lucasgomes.features.menuItem.ui.model.ConfigurationUiState
import eti.lucasgomes.features.menuItem.ui.model.MenuItemAction
import eti.lucasgomes.features.menuItem.ui.model.OptionUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.EmptyResult
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.math.BigDecimal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MenuItemViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val menuItemClient = mockk<MenuItemClient>()
    private val navigator = mockk<Navigator>(relaxed = true)
    private val cartRequest = slot<CartItemRequest>()

    private fun createViewModel(
        response: Resource<MenuItemResponse> = Resource.Success(menuItemResponse()),
        addToCartResult: EmptyResult = Resource.Success(Unit)
    ): MenuItemViewModel {
        coEvery { menuItemClient.getItem(MENU_ITEM_ID) } returns response
        coEvery { menuItemClient.addItemToCart(any(), any(), capture(cartRequest)) } returns
            addToCartResult
        return MenuItemViewModel(STORE_ID, MENU_ITEM_ID, navigator, menuItemClient)
    }

    private fun MenuItemViewModel.options(key: ConfigurationUiState) =
        uiState.value.configurations.getValue(key)

    @Test
    fun `initial fetch requests the menu item by id`() = runTest {
        createViewModel()

        coVerify(exactly = 1) { menuItemClient.getItem(MENU_ITEM_ID) }
    }

    @Test
    fun `initial state carries the menu item id and is loading while fetch is in flight`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            coEvery { menuItemClient.getItem(MENU_ITEM_ID) } coAnswers {
                gate.await()
                Resource.Success(menuItemResponse())
            }

            val viewModel = MenuItemViewModel(STORE_ID, MENU_ITEM_ID, navigator, menuItemClient)

            val state = viewModel.uiState.value
            assertEquals(MENU_ITEM_ID, state.id)
            assertTrue(state.isLoading)
            assertFalse(state.isLoaded)
            gate.complete(Unit)
        }

    @Test
    fun `initial fetch success maps item fields and marks loaded`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(UiText.PlainText("Margherita"), state.name)
        assertEquals(BigDecimal("12.50"), state.price)
        assertEquals("Tomato, mozzarella", state.ingredients)
        assertEquals("${MakaluConfig.BASE_URL}images/9", state.imageUrl)
        assertFalse(state.isLoading)
        assertTrue(state.isLoaded)
        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `initial fetch success keeps null ingredients and image url`() = runTest {
        val viewModel = createViewModel(
            Resource.Success(menuItemResponse(ingredients = null, imageId = null))
        )

        assertNull(viewModel.uiState.value.ingredients)
        assertNull(viewModel.uiState.value.imageUrl)
    }

    @Test
    fun `initial fetch success maps single choice configuration to unselected single choice options`() =
        runTest {
            val response = menuItemResponse(
                configurations = listOf(
                    singleChoiceConfiguration(
                        responseOption(10, "0.00"),
                        responseOption(11, "3.00")
                    )
                )
            )

            val viewModel = createViewModel(Resource.Success(response))

            val expected = listOf(
                OptionUiState.SingleChoice(10, "Option 10", BigDecimal("0.00"), isSelected = false),
                OptionUiState.SingleChoice(11, "Option 11", BigDecimal("3.00"), isSelected = false)
            )
            assertEquals(mapOf(singleKey to expected), viewModel.uiState.value.configurations)
        }

    @Test
    fun `initial fetch success maps multiple choice configuration to unselected multiple choice options`() =
        runTest {
            val response = menuItemResponse(
                configurations = listOf(
                    multipleChoiceConfiguration(
                        responseOption(20, "1.25"),
                        responseOption(21, "2.00")
                    )
                )
            )

            val viewModel = createViewModel(Resource.Success(response))

            val expected = listOf(
                OptionUiState.MultipleChoice(
                    20,
                    "Option 20",
                    BigDecimal("1.25"),
                    isSelected = false
                ),
                OptionUiState.MultipleChoice(
                    21,
                    "Option 21",
                    BigDecimal("2.00"),
                    isSelected = false
                )
            )
            assertEquals(mapOf(multipleKey to expected), viewModel.uiState.value.configurations)
        }

    @Test
    fun `initial fetch success maps quantity configuration to quantity options with zero amount`() =
        runTest {
            val response = menuItemResponse(
                configurations = listOf(quantityConfiguration(responseOption(30, "4.50")))
            )

            val viewModel = createViewModel(Resource.Success(response))

            val expected =
                listOf(OptionUiState.Quantity(30, "Option 30", BigDecimal("4.50"), amount = 0))
            assertEquals(mapOf(quantityKey to expected), viewModel.uiState.value.configurations)
        }

    @Test
    fun `initial fetch success keeps configurations in response order`() = runTest {
        val response = menuItemResponse(
            configurations = listOf(
                quantityConfiguration(),
                singleChoiceConfiguration(),
                multipleChoiceConfiguration()
            )
        )

        val viewModel = createViewModel(Resource.Success(response))

        assertEquals(
            listOf(quantityKey, singleKey, multipleKey),
            viewModel.uiState.value.configurations.keys.toList()
        )
    }

    @Test
    fun `initial fetch success with no configurations leaves configurations empty`() = runTest {
        val viewModel =
            createViewModel(Resource.Success(menuItemResponse(configurations = emptyList())))

        assertTrue(viewModel.uiState.value.configurations.isEmpty())
    }

    @Test
    fun `initial fetch failure sets general error and stops loading`() = runTest {
        val viewModel = createViewModel(Resource.Error(makaluError("MK-1", "Not found")))

        val state = viewModel.uiState.value
        assertEquals(UiText.PlainText("MK-1 - Not found"), state.generalError)
        assertFalse(state.isLoading)
        assertFalse(state.isLoaded)
    }

    @Test
    fun `dismiss error clears general error`() = runTest {
        val viewModel = createViewModel(Resource.Error(makaluError()))

        viewModel.onAction(MenuItemAction.OnDismissError)

        assertEquals(UiText.Empty, viewModel.uiState.value.generalError)
    }

    @Test
    fun `navigate back clicked navigates up`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.NavigateBackClicked)

        coVerify(exactly = 1) { navigator.navigateUp() }
    }

    @Test
    fun `selecting a single choice option selects it`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.OptionSelected(singleKey, 1))

        assertEquals(listOf(false, true, false), viewModel.options(singleKey).map { it.isSelected })
    }

    @Test
    fun `selecting another single choice option deselects the previous one`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.OptionSelected(singleKey, 0))

        viewModel.onAction(MenuItemAction.OptionSelected(singleKey, 2))

        assertEquals(listOf(false, false, true), viewModel.options(singleKey).map { it.isSelected })
    }

    @Test
    fun `selecting an already selected single choice option keeps it selected`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.OptionSelected(singleKey, 0))

        viewModel.onAction(MenuItemAction.OptionSelected(singleKey, 0))

        assertEquals(listOf(true, false, false), viewModel.options(singleKey).map { it.isSelected })
    }

    @Test
    fun `selecting a multiple choice option toggles it on`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.OptionSelected(multipleKey, 1))

        assertEquals(
            listOf(false, true, false),
            viewModel.options(multipleKey).map {
                it.isSelected
            }
        )
    }

    @Test
    fun `selecting a selected multiple choice option toggles it off`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.OptionSelected(multipleKey, 1))

        viewModel.onAction(MenuItemAction.OptionSelected(multipleKey, 1))

        assertEquals(
            listOf(false, false, false),
            viewModel.options(multipleKey).map {
                it.isSelected
            }
        )
    }

    @Test
    fun `selecting multiple choice options allows several selections`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.OptionSelected(multipleKey, 0))
        viewModel.onAction(MenuItemAction.OptionSelected(multipleKey, 2))

        assertEquals(
            listOf(true, false, true),
            viewModel.options(multipleKey).map {
                it.isSelected
            }
        )
    }

    @Test
    fun `selecting a quantity option leaves configurations unchanged`() = runTest {
        val viewModel = createViewModel()
        val before = viewModel.uiState.value.configurations

        viewModel.onAction(MenuItemAction.OptionSelected(quantityKey, 0))

        assertEquals(before, viewModel.uiState.value.configurations)
    }

    @Test
    fun `selecting an option only changes its own configuration`() = runTest {
        val viewModel = createViewModel()
        val multipleBefore = viewModel.options(multipleKey)
        val quantityBefore = viewModel.options(quantityKey)

        viewModel.onAction(MenuItemAction.OptionSelected(singleKey, 0))

        assertEquals(multipleBefore, viewModel.options(multipleKey))
        assertEquals(quantityBefore, viewModel.options(quantityKey))
    }

    @Test
    fun `quantity changed sets the amount of the quantity option`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.QuantityChanged(quantityKey, 1, 3))

        val amounts = viewModel.options(quantityKey).map { (it as OptionUiState.Quantity).amount }
        assertEquals(listOf(0, 3), amounts)
    }

    @Test
    fun `quantity changed above the maximum is clamped to 50`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.QuantityChanged(quantityKey, 0, 51))

        assertEquals(50, (viewModel.options(quantityKey)[0] as OptionUiState.Quantity).amount)
    }

    @Test
    fun `quantity changed below zero is clamped to 0`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.QuantityChanged(quantityKey, 0, 3))

        viewModel.onAction(MenuItemAction.QuantityChanged(quantityKey, 0, -1))

        assertEquals(0, (viewModel.options(quantityKey)[0] as OptionUiState.Quantity).amount)
    }

    @Test
    fun `quantity changed on a non quantity option leaves it unchanged`() = runTest {
        val viewModel = createViewModel()
        val before = viewModel.uiState.value.configurations

        viewModel.onAction(MenuItemAction.QuantityChanged(singleKey, 0, 5))

        assertEquals(before, viewModel.uiState.value.configurations)
    }

    @Test
    fun `notes changed updates notes`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.NotesChanged("No onions"))

        assertEquals("No onions", viewModel.uiState.value.notes)
    }

    @Test
    fun `notes with exactly 240 characters are accepted`() = runTest {
        val viewModel = createViewModel()
        val notes = "a".repeat(240)

        viewModel.onAction(MenuItemAction.NotesChanged(notes))

        assertEquals(notes, viewModel.uiState.value.notes)
    }

    @Test
    fun `notes longer than 240 characters are ignored and previous notes kept`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.NotesChanged("abc"))

        viewModel.onAction(MenuItemAction.NotesChanged("a".repeat(241)))

        assertEquals("abc", viewModel.uiState.value.notes)
    }

    @Test
    fun `add to cart is ignored when the item failed to load`() = runTest {
        val viewModel = createViewModel(Resource.Error(makaluError()))

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        coVerify(exactly = 0) { menuItemClient.addItemToCart(any(), any(), any()) }
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `add to cart is ignored while initial fetch is in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { menuItemClient.getItem(MENU_ITEM_ID) } coAnswers {
            gate.await()
            Resource.Success(menuItemResponse())
        }
        coEvery { menuItemClient.addItemToCart(any(), any(), any()) } returns Resource.Success(Unit)
        val viewModel = MenuItemViewModel(STORE_ID, MENU_ITEM_ID, navigator, menuItemClient)

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        coVerify(exactly = 0) { menuItemClient.addItemToCart(any(), any(), any()) }
        gate.complete(Unit)
    }

    @Test
    fun `add to cart sets loading while the request is in flight`() = runTest {
        val viewModel = createViewModel()
        val gate = CompletableDeferred<Unit>()
        coEvery { menuItemClient.addItemToCart(any(), any(), any()) } coAnswers {
            gate.await()
            Resource.Success(Unit)
        }

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        assertTrue(viewModel.uiState.value.isLoading)
        gate.complete(Unit)
    }

    @Test
    fun `add to cart is ignored while a previous request is in flight`() = runTest {
        val viewModel = createViewModel()
        val gate = CompletableDeferred<Unit>()
        coEvery { menuItemClient.addItemToCart(any(), any(), any()) } coAnswers {
            gate.await()
            Resource.Success(Unit)
        }
        viewModel.onAction(MenuItemAction.AddToCartClicked)

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        coVerify(exactly = 1) { menuItemClient.addItemToCart(any(), any(), any()) }
        gate.complete(Unit)
    }

    @Test
    fun `add to cart sends store id and menu item id`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        coVerify(exactly = 1) { menuItemClient.addItemToCart(STORE_ID, MENU_ITEM_ID, any()) }
    }

    @Test
    fun `add to cart sends no configurations when nothing is selected`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        assertEquals(
            emptyList<CartItemRequest.Configuration>(),
            cartRequest.captured.configurations
        )
    }

    @Test
    fun `add to cart sends selected single and multiple choice options with quantity 1`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onAction(MenuItemAction.OptionSelected(singleKey, 1))
            viewModel.onAction(MenuItemAction.OptionSelected(multipleKey, 0))
            viewModel.onAction(MenuItemAction.OptionSelected(multipleKey, 2))

            viewModel.onAction(MenuItemAction.AddToCartClicked)

            val expected = listOf(
                CartItemRequest.Configuration(11, 1),
                CartItemRequest.Configuration(20, 1),
                CartItemRequest.Configuration(22, 1)
            )
            assertEquals(expected, cartRequest.captured.configurations)
        }

    @Test
    fun `add to cart sends quantity options with their amount and skips zero amounts`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.QuantityChanged(quantityKey, 0, 3))

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        assertEquals(
            listOf(CartItemRequest.Configuration(30, 3)),
            cartRequest.captured.configurations
        )
    }

    @Test
    fun `add to cart sends trimmed notes`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.NotesChanged("  extra spicy  "))

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        assertEquals("extra spicy", cartRequest.captured.notes)
    }

    @Test
    fun `add to cart sends null notes when notes are blank`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(MenuItemAction.NotesChanged("   "))

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        assertNull(cartRequest.captured.notes)
    }

    @Test
    fun `add to cart sends null notes when notes were never entered`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        assertNull(cartRequest.captured.notes)
    }

    @Test
    fun `add to cart success stops loading and navigates to the store cart`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { navigator.navigate(Destination.Screen.Cart(STORE_ID), any()) }
    }

    @Test
    fun `add to cart failure sets general error, stops loading and does not navigate`() = runTest {
        val viewModel = createViewModel(
            addToCartResult = Resource.Error(makaluError("MK-2", "Out of stock"))
        )

        viewModel.onAction(MenuItemAction.AddToCartClicked)

        val state = viewModel.uiState.value
        assertEquals(UiText.PlainText("MK-2 - Out of stock"), state.generalError)
        assertFalse(state.isLoading)
        assertTrue(state.isLoaded)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }
}
