package eti.lucasgomes.makalu.features.home.ui

import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.home.HomeClient
import eti.lucasgomes.makalu.features.home.R
import eti.lucasgomes.makalu.features.home.ui.model.CategoryUiState
import eti.lucasgomes.makalu.features.home.ui.model.HomeAction
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState
import eti.lucasgomes.makalu.shared.model.CategoryResponse
import eti.lucasgomes.makalu.shared.model.StoreResponse
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.settings.Settings
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val homeClient = mockk<HomeClient>()
    private val navigator = mockk<Navigator>(relaxed = true)
    private var dataStore = FakeSettingsDataStore()

    @Before
    fun setUp() {
        coEvery { homeClient.getSelfAddress() } returns Resource.Success(addressResponse())
        coEvery { homeClient.getCategories() } returns Resource.Success(
            listOf(categoryResponse(1, "Pizza"), categoryResponse(2, "Coffee"))
        )
        coEvery { homeClient.getStores(any()) } returns Resource.Success(listOf(storeResponse()))
    }

    private fun createViewModel() = HomeViewModel(navigator, homeClient, dataStore)

    @Test
    fun `init shows loading for address filters and stores while requests are pending`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { homeClient.getSelfAddress() } coAnswers {
            gate.await()
            Resource.Success(addressResponse())
        }
        coEvery { homeClient.getCategories() } coAnswers {
            gate.await()
            Resource.Success(emptyList<CategoryResponse>())
        }
        coEvery { homeClient.getStores(any()) } coAnswers {
            gate.await()
            Resource.Success(emptyList<StoreResponse>())
        }

        val state = createViewModel().uiState.value

        assertTrue(state.isAddressLoading)
        assertTrue(state.isFiltersLoading)
        assertTrue(state.isStoresLoading)
        assertEquals(listOf(StoreUiState.Loading), state.stores)
    }

    @Test
    fun `init fetches stores without category filter`() = runTest {
        createViewModel()

        coVerify { homeClient.getStores(emptyList()) }
    }

    @Test
    fun `address success persists address id and label to settings`() = runTest {
        dataStore =
            FakeSettingsDataStore(Settings(accessToken = "access", refreshToken = "refresh"))

        createViewModel()

        assertEquals(
            Settings(
                accessToken = "access",
                refreshToken = "refresh",
                addressId = 5,
                addressName = "Main St, 12"
            ),
            dataStore.current
        )
    }

    @Test
    fun `address success shows formatted address and stops loading`() = runTest {
        val state = createViewModel().uiState.value

        assertEquals(UiText.PlainText("Main St, 12"), state.address)
        assertFalse(state.isAddressLoading)
    }

    @Test
    fun `address success without number shows only street`() = runTest {
        coEvery { homeClient.getSelfAddress() } returns
            Resource.Success(addressResponse(number = null))

        val state = createViewModel().uiState.value

        assertEquals(UiText.PlainText("Main St"), state.address)
    }

    @Test
    fun `address error falls back to stored address name`() = runTest {
        val settings = Settings(addressId = 2, addressName = "Old St, 1")
        dataStore = FakeSettingsDataStore(settings)
        coEvery { homeClient.getSelfAddress() } returns Resource.Error(makaluError(404))

        val state = createViewModel().uiState.value

        assertEquals(UiText.PlainText("Old St, 1"), state.address)
        assertFalse(state.isAddressLoading)
        assertEquals(settings, dataStore.current)
    }

    @Test
    fun `address error without stored address asks to add a delivery address`() = runTest {
        coEvery { homeClient.getSelfAddress() } returns Resource.Error(makaluError(404))

        val state = createViewModel().uiState.value

        assertEquals(UiText.StringResource(R.string.add_a_delivery_address), state.address)
        assertFalse(state.isAddressLoading)
    }

    @Test
    fun `blank stored address asks to add a delivery address`() = runTest {
        dataStore = FakeSettingsDataStore(Settings(addressName = "  "))
        coEvery { homeClient.getSelfAddress() } returns Resource.Error(makaluError(404))

        val state = createViewModel().uiState.value

        assertEquals(UiText.StringResource(R.string.add_a_delivery_address), state.address)
    }

    @Test
    fun `address shown follows later settings changes`() = runTest {
        val viewModel = createViewModel()

        dataStore.updateData { it.copy(addressName = "New St, 9") }

        assertEquals(UiText.PlainText("New St, 9"), viewModel.uiState.value.address)
    }

    @Test
    fun `categories success maps to unselected category ui states and stops loading`() = runTest {
        val state = createViewModel().uiState.value

        assertEquals(
            listOf(
                CategoryUiState(1, UiText.PlainText("Pizza"), false),
                CategoryUiState(2, UiText.PlainText("Coffee"), false)
            ),
            state.categories
        )
        assertFalse(state.isFiltersLoading)
    }

    @Test
    fun `categories error stops filters loading with no categories`() = runTest {
        coEvery { homeClient.getCategories() } returns Resource.Error(makaluError())

        val state = createViewModel().uiState.value

        assertFalse(state.isFiltersLoading)
        assertTrue(state.categories.isEmpty())
    }

    @Test
    fun `stores success maps to store data and stops loading`() = runTest {
        coEvery { homeClient.getStores(any()) } returns Resource.Success(
            listOf(
                storeResponse(
                    id = 9,
                    name = "Scary's",
                    categories = listOf(
                        categoryResponse(1, "Burger"),
                        categoryResponse(2, "Fries")
                    ),
                    logoImageId = 3,
                    coverImageId = null
                )
            )
        )

        val state = createViewModel().uiState.value

        assertEquals(
            listOf(StoreUiState.Data(9, "Scary's", "Burger, Fries", 3, null)),
            state.stores
        )
        assertFalse(state.isStoresLoading)
    }

    @Test
    fun `store without categories has empty category text`() = runTest {
        coEvery { homeClient.getStores(any()) } returns Resource.Success(
            listOf(storeResponse(categories = emptyList()))
        )

        val store = createViewModel().uiState.value.stores.single() as StoreUiState.Data

        assertEquals("", store.category)
    }

    @Test
    fun `empty stores result shows no content`() = runTest {
        coEvery { homeClient.getStores(any()) } returns Resource.Success(emptyList())

        val state = createViewModel().uiState.value

        assertEquals(listOf(StoreUiState.NoContent), state.stores)
        assertFalse(state.isStoresLoading)
    }

    @Test
    fun `stores error shows no content and stops loading`() = runTest {
        coEvery { homeClient.getStores(any()) } returns Resource.Error(makaluError())

        val state = createViewModel().uiState.value

        assertEquals(listOf(StoreUiState.NoContent), state.stores)
        assertFalse(state.isStoresLoading)
    }

    @Test
    fun `refresh stores sets stores loading while request is pending`() = runTest {
        val viewModel = createViewModel()
        val gate = CompletableDeferred<Unit>()
        coEvery { homeClient.getStores(any()) } coAnswers {
            gate.await()
            Resource.Success(emptyList<StoreResponse>())
        }

        viewModel.onAction(HomeAction.RefreshStores)

        assertTrue(viewModel.uiState.value.isStoresLoading)
    }

    @Test
    fun `refresh stores replaces stores with new result and stops loading`() = runTest {
        val viewModel = createViewModel()
        coEvery { homeClient.getStores(any()) } returns Resource.Success(
            listOf(
                storeResponse(id = 2, name = "Fresh", categories = emptyList(), logoImageId = null)
            )
        )

        viewModel.onAction(HomeAction.RefreshStores)

        val state = viewModel.uiState.value
        assertEquals(listOf(StoreUiState.Data(2, "Fresh", "", null, null)), state.stores)
        assertFalse(state.isStoresLoading)
    }

    @Test
    fun `refresh stores filters by selected categories`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HomeAction.CategoryClicked(1))

        viewModel.onAction(HomeAction.RefreshStores)

        coVerify(exactly = 2) { homeClient.getStores(listOf(2L)) }
    }

    @Test
    fun `refresh stores error shows no content`() = runTest {
        val viewModel = createViewModel()
        coEvery { homeClient.getStores(any()) } returns Resource.Error(makaluError())

        viewModel.onAction(HomeAction.RefreshStores)

        val state = viewModel.uiState.value
        assertEquals(listOf(StoreUiState.NoContent), state.stores)
        assertFalse(state.isStoresLoading)
    }

    @Test
    fun `store clicked navigates to store screen`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.StoreClicked(7))

        coVerify { navigator.navigate(Destination.Screen.Store(7)) }
    }

    @Test
    fun `address clicked navigates to address screen without id`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.AddressClicked)

        coVerify { navigator.navigate(Destination.Screen.Address()) }
    }

    @Test
    fun `auth clicked hides auth dialog and navigates to login`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.AuthClicked)

        assertFalse(viewModel.uiState.value.isAuthDialogVisible)
        coVerify { navigator.navigate(Destination.Screen.Login) }
    }

    @Test
    fun `auth dialog dismissed hides dialog without navigating`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.AuthDialogDismissed)

        assertFalse(viewModel.uiState.value.isAuthDialogVisible)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `category clicked selects only that category`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.CategoryClicked(1))

        assertEquals(listOf(false, true), viewModel.uiState.value.categories.map { it.isSelected })
    }

    @Test
    fun `category clicked twice deselects it`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.CategoryClicked(1))
        viewModel.onAction(HomeAction.CategoryClicked(1))

        assertEquals(listOf(false, false), viewModel.uiState.value.categories.map { it.isSelected })
    }

    @Test
    fun `category clicked fetches stores filtered by selected categories`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.CategoryClicked(0))
        viewModel.onAction(HomeAction.CategoryClicked(1))

        coVerify { homeClient.getStores(listOf(1L)) }
        coVerify { homeClient.getStores(listOf(1L, 2L)) }
    }

    @Test
    fun `category clicked shows filtered stores`() = runTest {
        coEvery { homeClient.getStores(listOf(1L)) } returns Resource.Success(
            listOf(
                storeResponse(
                    id = 4,
                    name = "Pizzeria",
                    categories = listOf(categoryResponse(1, "Pizza"))
                )
            )
        )
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.CategoryClicked(0))

        assertEquals(
            listOf(StoreUiState.Data(4, "Pizzeria", "Pizza", 3, null)),
            viewModel.uiState.value.stores
        )
    }

    @Test
    fun `deselecting last category fetches unfiltered stores`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.CategoryClicked(0))
        viewModel.onAction(HomeAction.CategoryClicked(0))

        coVerify(exactly = 2) { homeClient.getStores(emptyList()) }
    }
}
