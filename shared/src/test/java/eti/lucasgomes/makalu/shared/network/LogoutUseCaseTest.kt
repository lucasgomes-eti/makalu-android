package eti.lucasgomes.makalu.shared.network

import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.settings.FakeSettingsDataStore
import eti.lucasgomes.makalu.shared.settings.Settings
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LogoutUseCaseTest {

    private val navigator = mockk<Navigator>(relaxed = true)

    private val loggedInSettings = Settings(
        accessToken = "access",
        refreshToken = "refresh",
        addressId = 7,
        addressName = "Home"
    )

    private val authNavOptions = NavOptions(
        popUpTo = PopUpToOptions(Destination.Graph.Root, inclusive = true)
    )

    @Test
    fun `clears tokens and selected address`() = runTest {
        val dataStore = FakeSettingsDataStore(loggedInSettings)

        LogoutUseCase(dataStore, navigator)()

        assertEquals(Settings(), dataStore.current)
    }

    @Test
    fun `navigates to the auth graph popping the root graph inclusively when logged in`() = runTest {
        val dataStore = FakeSettingsDataStore(Settings(accessToken = "access"))

        LogoutUseCase(dataStore, navigator)()

        coVerify(exactly = 1) { navigator.navigate(Destination.Graph.Auth, authNavOptions) }
    }

    @Test
    fun `navigates when only a refresh token was stored`() = runTest {
        val dataStore = FakeSettingsDataStore(Settings(refreshToken = "refresh"))

        LogoutUseCase(dataStore, navigator)()

        coVerify(exactly = 1) { navigator.navigate(Destination.Graph.Auth, authNavOptions) }
    }

    @Test
    fun `does not navigate when no token was stored`() = runTest {
        val dataStore = FakeSettingsDataStore(Settings(addressId = 7, addressName = "Home"))

        LogoutUseCase(dataStore, navigator)()

        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
        assertEquals(Settings(), dataStore.current)
    }

    @Test
    fun `completes logout even if the calling coroutine is cancelled`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val dataStore = FakeSettingsDataStore(loggedInSettings, updateGate = gate)
        val useCase = LogoutUseCase(dataStore, navigator)
        val job = launch { useCase() }
        runCurrent()

        job.cancel()
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(Settings(), dataStore.current)
        coVerify(exactly = 1) { navigator.navigate(Destination.Graph.Auth, authNavOptions) }
    }
}
