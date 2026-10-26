package eti.lucasgomes.features.cart.ui

import eti.lucasgomes.features.cart.CartClient
import eti.lucasgomes.features.cart.STORE_ID
import eti.lucasgomes.features.cart.cartItemResponse
import eti.lucasgomes.features.cart.cartResponse
import eti.lucasgomes.features.cart.model.CartResponse
import eti.lucasgomes.features.cart.model.CreateOrderRequest
import eti.lucasgomes.features.cart.testError
import eti.lucasgomes.makalu.components.ItemConfigurationUiState
import eti.lucasgomes.makalu.components.ItemOptionUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderStatus
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.math.BigDecimal
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cartClient = mockk<CartClient>()
    private val navigator = mockk<Navigator>(relaxed = true)

    private fun createViewModel() = CartViewModel(STORE_ID, cartClient, navigator)

    private fun givenCart(response: CartResponse = twoItemCart()) {
        coEvery { cartClient.getByStore(STORE_ID) } returns Resource.Success(response)
    }

    private fun twoItemCart() = cartResponse(
        items = listOf(cartItemResponse(id = 1L), cartItemResponse(id = 2L, name = "Soda"))
    )

    private fun dataState(viewModel: CartViewModel) = viewModel.uiState.value as CartUiState.Data

    private fun orderResponse(id: Long) = OrderSimpleResponse(
        id = id,
        status = OrderStatus.PENDING,
        orderNumber = "0001",
        storeName = "Store",
        storeImageId = null,
        deliveryAddressLine = "Rua A, 10",
        itemsCount = 1,
        total = OrderSimpleResponse.Total(BigDecimal("5.00"), BigDecimal("15.50")),
        createdAt = Instant.parse("2026-01-02T10:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T10:00:00Z")
    )

    @Test
    fun `starts in loading while the cart is being fetched`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { cartClient.getByStore(STORE_ID) } coAnswers {
            gate.await()
            Resource.Success(cartResponse())
        }

        val viewModel = createViewModel()

        assertEquals(CartUiState.Loading, viewModel.uiState.value)
        gate.complete(Unit)
    }

    @Test
    fun `initial fetch requests the cart of the given store`() = runTest {
        givenCart()

        createViewModel()

        coVerify(exactly = 1) { cartClient.getByStore(STORE_ID) }
    }

    @Test
    fun `initial fetch maps cart items into data state`() = runTest {
        val option = CartResponse.CartItemResponse.Configuration.Option(
            id = 11L,
            name = "Large",
            additionalPrice = BigDecimal("2.50"),
            quantity = 2
        )
        val configuration = CartResponse.CartItemResponse.Configuration(
            configurationId = 10L,
            name = "Size",
            options = listOf(option)
        )
        givenCart(
            cartResponse(items = listOf(cartItemResponse(configurations = listOf(configuration))))
        )

        val viewModel = createViewModel()

        val expected = CartItemUiState(
            id = 1L,
            imageId = 7L,
            name = "Pizza",
            price = BigDecimal("10.50"),
            notes = "No onions",
            isRemoving = false,
            configurations = listOf(
                ItemConfigurationUiState(
                    10L,
                    "Size",
                    listOf(ItemOptionUiState(11L, "Large", BigDecimal("2.50"), 2))
                )
            )
        )
        assertEquals(listOf(expected), dataState(viewModel).items)
    }

    @Test
    fun `initial fetch maps delivery fee and total price`() = runTest {
        givenCart(cartResponse(deliveryFee = BigDecimal("5.00"), total = BigDecimal("25.50")))

        val viewModel = createViewModel()

        val state = dataState(viewModel)
        assertEquals(BigDecimal("5.00"), state.deliveryFee)
        assertEquals(BigDecimal("25.50"), state.totalPrice)
        assertEquals(false, state.isMakingOrder)
    }

    @Test
    fun `initial fetch uses delivery address line as address`() = runTest {
        givenCart(cartResponse(deliveryAddressLine = "Rua A, 10"))

        val viewModel = createViewModel()

        assertEquals(UiText.PlainText("Rua A, 10"), dataState(viewModel).address)
    }

    @Test
    fun `initial fetch without delivery address falls back to address not found`() = runTest {
        givenCart(cartResponse(deliveryAddressLine = null))

        val viewModel = createViewModel()

        assertEquals(UiText.PlainText("Address not found"), dataState(viewModel).address)
    }

    @Test
    fun `initial fetch with no items shows empty state`() = runTest {
        givenCart(cartResponse(items = emptyList()))

        val viewModel = createViewModel()

        assertEquals(CartUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `initial fetch failure shows error with formatted message`() = runTest {
        coEvery { cartClient.getByStore(STORE_ID) } returns Resource.Error(testError)

        val viewModel = createViewModel()

        assertEquals(CartUiState.Error(UiText.PlainText("MK-1 - boom")), viewModel.uiState.value)
    }

    @Test
    fun `remove item marks only that item as removing while the request is in flight`() = runTest {
        givenCart()
        val gate = CompletableDeferred<Unit>()
        coEvery { cartClient.deleteByItem(STORE_ID, 1L) } coAnswers {
            gate.await()
            Resource.Success(cartResponse(items = listOf(cartItemResponse(id = 2L))))
        }
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.RemoveItemClicked(1L))

        val removing = dataState(viewModel).items.associate { it.id to it.isRemoving }
        assertEquals(mapOf(1L to true, 2L to false), removing)
        gate.complete(Unit)
    }

    @Test
    fun `remove item calls the client with store and cart item ids`() = runTest {
        givenCart()
        coEvery { cartClient.deleteByItem(any(), any()) } returns
            Resource.Success(cartResponse(items = listOf(cartItemResponse(id = 2L))))
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.RemoveItemClicked(1L))

        coVerify(exactly = 1) { cartClient.deleteByItem(STORE_ID, 1L) }
    }

    @Test
    fun `remove item success drops the item and updates the total price`() = runTest {
        givenCart()
        coEvery { cartClient.deleteByItem(STORE_ID, 1L) } returns Resource.Success(
            cartResponse(
                items = listOf(cartItemResponse(id = 2L, name = "Soda")),
                total = BigDecimal("15.00")
            )
        )
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.RemoveItemClicked(1L))

        val state = dataState(viewModel)
        assertEquals(listOf(2L), state.items.map { it.id })
        assertEquals(BigDecimal("15.00"), state.totalPrice)
    }

    @Test
    fun `remove item success with an empty cart response shows empty state`() = runTest {
        givenCart()
        coEvery { cartClient.deleteByItem(STORE_ID, 1L) } returns
            Resource.Success(cartResponse(items = emptyList()))
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.RemoveItemClicked(1L))

        assertEquals(CartUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `remove item success for an item not in the list keeps the state unchanged`() = runTest {
        givenCart()
        coEvery { cartClient.deleteByItem(STORE_ID, 99L) } returns
            Resource.Success(cartResponse(total = BigDecimal("99.00")))
        val viewModel = createViewModel()
        val before = viewModel.uiState.value

        viewModel.onAction(CartAction.RemoveItemClicked(99L))

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun `remove item success outside data state keeps the current state`() = runTest {
        givenCart(cartResponse(items = emptyList()))
        coEvery { cartClient.deleteByItem(STORE_ID, 1L) } returns Resource.Success(cartResponse())
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.RemoveItemClicked(1L))

        assertEquals(CartUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `delete all is ignored when the cart is not loaded`() = runTest {
        givenCart(cartResponse(items = emptyList()))
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.DeleteAllItemsClicked)

        assertEquals(CartUiState.Empty, viewModel.uiState.value)
        coVerify(exactly = 0) { cartClient.deleteAllItemsByStore(any()) }
    }

    @Test
    fun `delete all shows loading while the request is in flight`() = runTest {
        givenCart()
        val gate = CompletableDeferred<Unit>()
        coEvery { cartClient.deleteAllItemsByStore(STORE_ID) } coAnswers {
            gate.await()
            Resource.Success(Unit)
        }
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.DeleteAllItemsClicked)

        assertEquals(CartUiState.Loading, viewModel.uiState.value)
        gate.complete(Unit)
    }

    @Test
    fun `delete all success shows empty state`() = runTest {
        givenCart()
        coEvery { cartClient.deleteAllItemsByStore(STORE_ID) } returns Resource.Success(Unit)
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.DeleteAllItemsClicked)

        assertEquals(CartUiState.Empty, viewModel.uiState.value)
        coVerify(exactly = 1) { cartClient.deleteAllItemsByStore(STORE_ID) }
    }

    @Test
    fun `delete all failure shows error with formatted message`() = runTest {
        givenCart()
        coEvery { cartClient.deleteAllItemsByStore(STORE_ID) } returns Resource.Error(testError)
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.DeleteAllItemsClicked)

        assertEquals(CartUiState.Error(UiText.PlainText("MK-1 - boom")), viewModel.uiState.value)
    }

    @Test
    fun `make order is ignored when the cart is not loaded`() = runTest {
        coEvery { cartClient.getByStore(STORE_ID) } returns Resource.Error(testError)
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.MakeOrderClicked)

        coVerify(exactly = 0) { cartClient.submitOrder(any()) }
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `make order sets making order flag while submitting`() = runTest {
        givenCart()
        val gate = CompletableDeferred<Unit>()
        coEvery { cartClient.submitOrder(any()) } coAnswers {
            gate.await()
            Resource.Success(orderResponse(77L))
        }
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.MakeOrderClicked)

        assertTrue(dataState(viewModel).isMakingOrder)
        gate.complete(Unit)
    }

    @Test
    fun `make order submits the store id and the fetched cart id`() = runTest {
        givenCart(cartResponse(id = 42L))
        coEvery { cartClient.submitOrder(any()) } returns Resource.Success(orderResponse(77L))
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.MakeOrderClicked)

        coVerify(exactly = 1) {
            cartClient.submitOrder(CreateOrderRequest(storeId = STORE_ID, cartId = 42L))
        }
    }

    @Test
    fun `make order success navigates to order detail popping up to the root graph`() = runTest {
        givenCart()
        coEvery { cartClient.submitOrder(any()) } returns Resource.Success(orderResponse(77L))
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.MakeOrderClicked)

        coVerify(exactly = 1) {
            navigator.navigate(
                Destination.Screen.OderDetail(77L),
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Root))
            )
        }
    }

    @Test
    fun `make order failure shows error and does not navigate`() = runTest {
        givenCart()
        coEvery { cartClient.submitOrder(any()) } returns Resource.Error(testError)
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.MakeOrderClicked)

        assertEquals(CartUiState.Error(UiText.PlainText("MK-1 - boom")), viewModel.uiState.value)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `make order is ignored while an order is already being submitted`() = runTest {
        givenCart()
        val gate = CompletableDeferred<Unit>()
        coEvery { cartClient.submitOrder(any()) } coAnswers {
            gate.await()
            Resource.Success(orderResponse(77L))
        }
        val viewModel = createViewModel()

        viewModel.onAction(CartAction.MakeOrderClicked)
        viewModel.onAction(CartAction.MakeOrderClicked)

        coVerify(exactly = 1) { cartClient.submitOrder(any()) }
        gate.complete(Unit)
    }
}
