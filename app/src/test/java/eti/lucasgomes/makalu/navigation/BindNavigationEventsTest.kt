package eti.lucasgomes.makalu.navigation

import android.app.Application
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.NavigationAction
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlin.coroutines.EmptyCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class BindNavigationEventsTest {

    private val navHostController = mockk<NavHostController>(relaxed = true)

    @Test
    fun `navigate without nav options navigates to the destination without pop up`() {
        val builder = captureNavigateBuilder(Destination.Screen.Home)

        bind()(NavigationAction.Navigate(Destination.Screen.Home, null))

        val options = navOptions(builder.captured)
        verify(exactly = 1) {
            navHostController.navigate(Destination.Screen.Home, any<NavOptionsBuilder.() -> Unit>())
        }
        assertNull(options.popUpToRouteObject)
        assertEquals(-1, options.popUpToId)
    }

    @Test
    fun `navigate with nav options but no pop up does not pop up`() {
        val builder = captureNavigateBuilder(Destination.Screen.Orders)

        bind()(NavigationAction.Navigate(Destination.Screen.Orders, NavOptions(popUpTo = null)))

        val options = navOptions(builder.captured)
        assertNull(options.popUpToRouteObject)
        assertEquals(-1, options.popUpToId)
    }

    @Test
    fun `navigate with pop up applies pop up destination with default flags`() {
        val builder = captureNavigateBuilder(Destination.Screen.Home)

        bind()(
            NavigationAction.Navigate(
                Destination.Screen.Home,
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth))
            )
        )

        val options = navOptions(builder.captured)
        assertEquals(Destination.Graph.Auth, options.popUpToRouteObject)
        assertFalse(options.isPopUpToInclusive())
        assertFalse(options.shouldPopUpToSaveState())
    }

    @Test
    fun `navigate with inclusive pop up applies inclusive flag`() {
        val builder = captureNavigateBuilder(Destination.Graph.Root)

        bind()(
            NavigationAction.Navigate(
                Destination.Graph.Root,
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true))
            )
        )

        val options = navOptions(builder.captured)
        assertEquals(Destination.Graph.Auth, options.popUpToRouteObject)
        assertTrue(options.isPopUpToInclusive())
        assertFalse(options.shouldPopUpToSaveState())
    }

    @Test
    fun `navigate with save state pop up applies save state flag`() {
        val builder = captureNavigateBuilder(Destination.Graph.Auth)

        bind()(
            NavigationAction.Navigate(
                Destination.Graph.Auth,
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Root, saveState = true))
            )
        )

        val options = navOptions(builder.captured)
        assertEquals(Destination.Graph.Root, options.popUpToRouteObject)
        assertFalse(options.isPopUpToInclusive())
        assertTrue(options.shouldPopUpToSaveState())
    }

    @Test
    fun `navigate passes data class destinations unchanged`() {
        bind()(NavigationAction.Navigate(Destination.Screen.Store(42), null))

        verify(exactly = 1) {
            navHostController.navigate(
                Destination.Screen.Store(42),
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `navigate up calls navigate up only`() {
        bind()(NavigationAction.NavigateUp)

        verify(exactly = 1) { navHostController.navigateUp() }
        verify(exactly = 0) {
            navHostController.navigate(any<Destination>(), any<NavOptionsBuilder.() -> Unit>())
        }
    }

    @Test
    fun `navigate up with result stores result in previous entry and navigates up`() {
        val handle = SavedStateHandle()
        val previousEntry = mockk<NavBackStackEntry> { every { savedStateHandle } returns handle }
        every { navHostController.previousBackStackEntry } returns previousEntry

        bind()(
            NavigationAction.NavigateUpWithResult(RequestKey.ImageCroppedUriOutput, "content://img")
        )

        assertEquals("content://img", handle.get<String>("ImageCroppedUriOutput"))
        verifyOrder {
            previousEntry.savedStateHandle
            navHostController.navigateUp()
        }
    }

    @Test
    fun `navigate up with result without previous entry still navigates up`() {
        every { navHostController.previousBackStackEntry } returns null

        bind()(
            NavigationAction.NavigateUpWithResult(RequestKey.ImageCroppedUriOutput, "content://img")
        )

        verify(exactly = 1) { navHostController.navigateUp() }
    }

    private fun captureNavigateBuilder(destination: Destination) =
        slot<NavOptionsBuilder.() -> Unit>().also { builder ->
            every { navHostController.navigate(destination, capture(builder)) } returns Unit
        }

    private fun bind(): (NavigationAction) -> Unit {
        var handler: ((NavigationAction) -> Unit)? = null
        val composition = Composition(UnitApplier(), Recomposer(EmptyCoroutineContext))
        composition.setContent { handler = bindNavigationEvents(navHostController) }
        composition.dispose()
        return checkNotNull(handler)
    }

    private class UnitApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }
}
