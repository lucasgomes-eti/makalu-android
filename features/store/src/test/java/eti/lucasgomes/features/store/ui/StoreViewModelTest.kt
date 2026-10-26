package eti.lucasgomes.features.store.ui

import MakaluConfig
import eti.lucasgomes.features.store.StoreClient
import eti.lucasgomes.features.store.makaluError
import eti.lucasgomes.features.store.menuItemResponse
import eti.lucasgomes.features.store.model.MenuItemResponse
import eti.lucasgomes.features.store.storeResponse
import eti.lucasgomes.features.store.ui.model.MenuItemUiState
import eti.lucasgomes.features.store.ui.model.StoreAction
import eti.lucasgomes.makalu.components.R
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.model.StoreResponse
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import java.math.BigDecimal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val storeId = 3L
    private val client = mockk<StoreClient>()
    private val navigator = mockk<Navigator>(relaxed = true)

    private fun givenStore(result: Resource<StoreResponse>) {
        coEvery { client.getStore(storeId) } returns result
    }

    private fun givenMenu(result: Resource<List<MenuItemResponse>>) {
        coEvery { client.getMenuItems(storeId) } returns result
    }

    private fun givenSuccessfulFetch(
        store: StoreResponse = storeResponse(id = storeId),
        menu: List<MenuItemResponse> = listOf(menuItemResponse())
    ) {
        givenStore(Resource.Success(store))
        givenMenu(Resource.Success(menu))
    }

    private fun createViewModel() = StoreViewModel(storeId, client, navigator)

    @Test
    fun `initial state carries the store id`() = runTest {
        givenSuccessfulFetch()

        val viewModel = createViewModel()

        assertEquals(storeId, viewModel.uiState.value.id)
    }

    @Test
    fun `initial fetch requests store and menu with the store id`() = runTest {
        givenSuccessfulFetch()

        createViewModel()

        coVerifyOrder {
            client.getStore(storeId)
            client.getMenuItems(storeId)
        }
        coVerify(exactly = 1) { client.getStore(any()) }
        coVerify(exactly = 1) { client.getMenuItems(any()) }
    }

    @Test
    fun `initial fetch shows loading and loading title while store request is in flight`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            coEvery { client.getStore(storeId) } coAnswers {
                gate.await()
                Resource.Success(storeResponse(id = storeId))
            }
            givenMenu(Resource.Success(emptyList()))

            val viewModel = createViewModel()

            val state = viewModel.uiState.value
            assertTrue(state.isLoading)
            assertEquals(UiText.StringResource(R.string.loading), state.name)
            gate.complete(Unit)
        }

    @Test
    fun `store success keeps loading while menu request is in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        givenStore(Resource.Success(storeResponse(id = storeId, name = "Pizzeria")))
        coEvery { client.getMenuItems(storeId) } coAnswers {
            gate.await()
            Resource.Success(emptyList())
        }

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals(UiText.PlainText("Pizzeria"), state.name)
        assertEquals(BigDecimal("5.90"), state.deliveryFee)
        gate.complete(Unit)
    }

    @Test
    fun `initial fetch success populates store data and stops loading`() = runTest {
        givenSuccessfulFetch(
            store = storeResponse(
                id = storeId,
                name = "Burger Place",
                deliveryFee = BigDecimal("7.25")
            )
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UiText.PlainText("Burger Place"), state.name)
        assertEquals(BigDecimal("7.25"), state.deliveryFee)
        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `initial fetch success exposes cover image url from cover image id`() = runTest {
        givenSuccessfulFetch(store = storeResponse(id = storeId, coverImageId = 9))

        val viewModel = createViewModel()

        assertEquals("${MakaluConfig.BASE_URL}images/9", viewModel.uiState.value.coverImageUrl)
    }

    @Test
    fun `initial fetch success with null cover image id leaves cover image url null`() = runTest {
        givenSuccessfulFetch(store = storeResponse(id = storeId, coverImageId = null))

        val viewModel = createViewModel()

        assertNull(viewModel.uiState.value.coverImageUrl)
    }

    @Test
    fun `menu items are grouped by category preserving first appearance order`() = runTest {
        givenSuccessfulFetch(
            menu = listOf(
                menuItemResponse(id = 1, category = "Pizza", name = "Margherita"),
                menuItemResponse(id = 2, category = "Drinks", name = "Cola"),
                menuItemResponse(id = 3, category = "Pizza", name = "Pepperoni")
            )
        )

        val viewModel = createViewModel()

        val menuItems = viewModel.uiState.value.menuItems
        assertEquals(listOf("Pizza", "Drinks"), menuItems.keys.toList())
        assertEquals(listOf(1L, 3L), menuItems.getValue("Pizza").map { it.id })
        assertEquals(listOf(2L), menuItems.getValue("Drinks").map { it.id })
    }

    @Test
    fun `menu items are mapped to ui state fields`() = runTest {
        givenSuccessfulFetch(
            menu = listOf(
                menuItemResponse(
                    id = 1,
                    category = "Pizza",
                    name = "Margherita",
                    price = BigDecimal("12.50"),
                    imageId = 4
                ),
                menuItemResponse(id = 2, category = "Pizza", name = "Plain", imageId = null)
            )
        )

        val viewModel = createViewModel()

        val items = viewModel.uiState.value.menuItems.getValue("Pizza")
        assertEquals(
            MenuItemUiState(
                id = 1,
                category = "Pizza",
                name = "Margherita",
                price = BigDecimal("12.50"),
                imageId = 4
            ),
            items[0]
        )
        assertEquals("${MakaluConfig.BASE_URL}images/4", items[0].imageUrl)
        assertNull(items[1].imageUrl)
    }

    @Test
    fun `empty menu yields empty menu items map`() = runTest {
        givenSuccessfulFetch(menu = emptyList())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.menuItems.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `menu failure after store success sets general error and stops loading`() = runTest {
        val error =
            makaluError(httpCode = 500, message = "Menu unavailable", internalCode = "MK-500")
        givenStore(Resource.Success(storeResponse(id = storeId, name = "Pizzeria")))
        givenMenu(Resource.Error(error))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UiText.PlainText("MK-500 - Menu unavailable"), state.generalError)
        assertEquals(UiText.PlainText("Pizzeria"), state.name)
        assertEquals(BigDecimal("5.90"), state.deliveryFee)
        assertTrue(state.menuItems.isEmpty())
    }

    @Test
    fun `store failure sets general error and stops loading`() = runTest {
        val error = makaluError()
        givenStore(Resource.Error(error))
        givenMenu(Resource.Success(listOf(menuItemResponse())))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(UiText.PlainText(error.formatedMessage), state.generalError)
        assertFalse(state.isLoading)
        assertEquals(BigDecimal.ZERO, state.deliveryFee)
    }

    @Test
    fun `store failure still fetches and shows menu items`() = runTest {
        givenStore(Resource.Error(makaluError()))
        givenMenu(Resource.Success(listOf(menuItemResponse(id = 1, category = "Pizza"))))

        val viewModel = createViewModel()

        coVerify(exactly = 1) { client.getMenuItems(storeId) }
        assertEquals(listOf(1L), viewModel.uiState.value.menuItems.getValue("Pizza").map { it.id })
    }

    @Test
    fun `OnDismissError clears general error`() = runTest {
        givenStore(Resource.Success(storeResponse(id = storeId)))
        givenMenu(Resource.Error(makaluError()))
        val viewModel = createViewModel()
        val before = viewModel.uiState.value

        viewModel.onAction(StoreAction.OnDismissError)

        assertEquals(before.copy(generalError = UiText.Empty), viewModel.uiState.value)
    }

    @Test
    fun `NavigateBackClicked navigates up`() = runTest {
        givenSuccessfulFetch()
        val viewModel = createViewModel()

        viewModel.onAction(StoreAction.NavigateBackClicked)

        coVerify(exactly = 1) { navigator.navigateUp() }
    }

    @Test
    fun `MenuItemClicked navigates to menu item with store and item ids`() = runTest {
        givenSuccessfulFetch()
        val viewModel = createViewModel()

        viewModel.onAction(StoreAction.MenuItemClicked(5))

        coVerify(exactly = 1) {
            navigator.navigate(Destination.Screen.MenuItem(storeId = storeId, menuItemId = 5))
        }
    }

    @Test
    fun `CartClicked navigates to cart of the store`() = runTest {
        givenSuccessfulFetch()
        val viewModel = createViewModel()

        viewModel.onAction(StoreAction.CartClicked)

        coVerify(exactly = 1) { navigator.navigate(Destination.Screen.Cart(storeId = storeId)) }
    }

    @Test
    fun `actions do not trigger additional fetches`() = runTest {
        givenSuccessfulFetch()
        val viewModel = createViewModel()

        viewModel.onAction(StoreAction.OnDismissError)
        viewModel.onAction(StoreAction.NavigateBackClicked)
        viewModel.onAction(StoreAction.MenuItemClicked(5))
        viewModel.onAction(StoreAction.CartClicked)

        coVerify(exactly = 1) { client.getStore(any()) }
        coVerify(exactly = 1) { client.getMenuItems(any()) }
    }
}
