package eti.lucasgomes.makalu.features.orders.list

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.features.orders.orderSimpleResponse
import eti.lucasgomes.makalu.shared.model.OrderSimpleResponse
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OrdersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ordersClient = mockk<OrdersClient>()
    private val navigator = mockk<Navigator>(relaxed = true)
    private val error = MakaluError(404, "Not found", "MK-404")

    private fun givenOrders(result: Resource<List<OrderSimpleResponse>>) {
        coEvery { ordersClient.getOrders() } returns result
    }

    private fun createViewModel() = OrdersViewModel(ordersClient, navigator)

    @Test
    fun `init fetches the orders once`() = runTest {
        givenOrders(Resource.Success(emptyList()))

        createViewModel()

        coVerify(exactly = 1) { ordersClient.getOrders() }
    }

    @Test
    fun `initial fetch shows loading while the request is in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { ordersClient.getOrders() } coAnswers {
            gate.await()
            Resource.Success(emptyList())
        }

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(UiText.Empty, viewModel.uiState.value.generalError)

        gate.complete(Unit)

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial fetch success fills the orders and stops loading`() = runTest {
        val responses = listOf(orderSimpleResponse(id = 1), orderSimpleResponse(id = 2))
        givenOrders(Resource.Success(responses))

        val state = createViewModel().uiState.value

        assertEquals(responses.toUiItems(), state.orders)
        assertFalse(state.isLoading)
        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `initial fetch success with no orders leaves the state empty`() = runTest {
        givenOrders(Resource.Success(emptyList()))

        val state = createViewModel().uiState.value

        assertTrue(state.orders.isEmpty())
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `initial fetch failure sets general error and stops loading`() = runTest {
        givenOrders(Resource.Error(error))

        val state = createViewModel().uiState.value

        assertEquals(UiText.PlainText("MK-404 - Not found"), state.generalError)
        assertFalse(state.isLoading)
        assertTrue(state.orders.isEmpty())
    }

    @Test
    fun `dismiss error clears the general error`() = runTest {
        givenOrders(Resource.Error(error))
        val viewModel = createViewModel()

        viewModel.onAction(OrdersAction.OnDismissError)

        assertEquals(UiText.Empty, viewModel.uiState.value.generalError)
    }

    @Test
    fun `dismiss error does not refetch`() = runTest {
        givenOrders(Resource.Error(error))
        val viewModel = createViewModel()

        viewModel.onAction(OrdersAction.OnDismissError)

        coVerify(exactly = 1) { ordersClient.getOrders() }
    }

    @Test
    fun `order click navigates to the order detail`() = runTest {
        givenOrders(Resource.Success(emptyList()))
        val viewModel = createViewModel()

        viewModel.onAction(OrdersAction.OnOrderClicked(42))

        coVerify(exactly = 1) { navigator.navigate(Destination.Screen.OderDetail(42), any()) }
    }

    @Test
    fun `order click does not change the ui state`() = runTest {
        givenOrders(Resource.Success(listOf(orderSimpleResponse(id = 42))))
        val viewModel = createViewModel()
        val before = viewModel.uiState.value

        viewModel.onAction(OrdersAction.OnOrderClicked(42))

        assertEquals(before, viewModel.uiState.value)
    }
}
