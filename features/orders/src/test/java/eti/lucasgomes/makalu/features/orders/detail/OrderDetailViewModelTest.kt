package eti.lucasgomes.makalu.features.orders.detail

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.features.orders.model.OrderDetailedResponse
import eti.lucasgomes.makalu.features.orders.orderDetailedResponse
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OrderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ordersClient = mockk<OrdersClient>()
    private val response = orderDetailedResponse(id = 42)
    private val error = MakaluError(500, "Boom", "MK-1")

    private fun givenOrder(result: Resource<OrderDetailedResponse>) {
        coEvery { ordersClient.getOrder(42) } returns result
    }

    private fun givenSuspendedOrder(gate: CompletableDeferred<Unit>) {
        coEvery { ordersClient.getOrder(42) } coAnswers {
            gate.await()
            Resource.Success(response)
        }
    }

    private fun createViewModel() = OrderDetailViewModel(42, ordersClient)

    @Test
    fun `initial state carries the order id`() = runTest {
        givenSuspendedOrder(CompletableDeferred())

        val viewModel = createViewModel()

        assertEquals(42L, viewModel.uiState.value.id)
    }

    @Test
    fun `init fetches the order with its id`() = runTest {
        givenOrder(Resource.Success(response))

        createViewModel()

        coVerify(exactly = 1) { ordersClient.getOrder(42) }
    }

    @Test
    fun `initial fetch shows loading while the request is in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        givenSuspendedOrder(gate)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals(UiText.Empty, state.generalError)
        assertNull(state.card)
        assertTrue(state.items.isEmpty())

        gate.complete(Unit)

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial fetch success fills the card and items`() = runTest {
        givenOrder(Resource.Success(response))

        val state = createViewModel().uiState.value

        assertEquals(response.toCardUiState(), state.card)
        assertEquals(response.toUiItems(), state.items)
        assertFalse(state.isLoading)
        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `initial fetch failure sets general error and stops loading`() = runTest {
        givenOrder(Resource.Error(error))

        val state = createViewModel().uiState.value

        assertEquals(UiText.PlainText("MK-1 - Boom"), state.generalError)
        assertFalse(state.isLoading)
        assertNull(state.card)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun `dismiss error clears the general error`() = runTest {
        givenOrder(Resource.Error(error))
        val viewModel = createViewModel()
        val before = viewModel.uiState.value

        viewModel.onAction(OrderDetailAction.OnDismissError)

        assertEquals(before.copy(generalError = UiText.Empty), viewModel.uiState.value)
    }

    @Test
    fun `dismiss error does not refetch`() = runTest {
        givenOrder(Resource.Error(error))
        val viewModel = createViewModel()

        viewModel.onAction(OrderDetailAction.OnDismissError)

        coVerify(exactly = 1) { ordersClient.getOrder(42) }
    }
}
