package eti.lucasgomes.makalu.shared.navigation

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultNavigatorTest {

    private val navigator = DefaultNavigator()

    @Test
    fun `navigate emits a navigate action with destination and options`() = runTest {
        val options = NavOptions(popUpTo = PopUpToOptions(Destination.Screen.Home))

        navigator.navigate(Destination.Screen.Store(3), options)

        assertEquals(
            NavigationAction.Navigate(Destination.Screen.Store(3), options),
            navigator.navigationActions.first()
        )
    }

    @Test
    fun `navigate without options emits null options`() = runTest {
        navigator.navigate(Destination.Screen.Home)

        assertEquals(
            NavigationAction.Navigate(Destination.Screen.Home, null),
            navigator.navigationActions.first()
        )
    }

    @Test
    fun `navigateUp emits navigate up`() = runTest {
        navigator.navigateUp()

        assertEquals(NavigationAction.NavigateUp, navigator.navigationActions.first())
    }

    @Test
    fun `navigateUpWithResult emits the request key and result`() = runTest {
        navigator.navigateUpWithResult(RequestKey.ImageCroppedUriOutput, "content://x")

        assertEquals(
            NavigationAction.NavigateUpWithResult(RequestKey.ImageCroppedUriOutput, "content://x"),
            navigator.navigationActions.first()
        )
    }

    @Test
    fun `buffers actions sent before anyone collects and delivers them in order`() = runTest {
        navigator.navigate(Destination.Screen.Orders)
        navigator.navigateUp()
        navigator.navigateUpWithResult(RequestKey.ImageCroppedUriOutput, 1)

        val actions = navigator.navigationActions.take(3).toList()

        assertEquals(
            listOf(
                NavigationAction.Navigate(Destination.Screen.Orders, null),
                NavigationAction.NavigateUp,
                NavigationAction.NavigateUpWithResult(RequestKey.ImageCroppedUriOutput, 1)
            ),
            actions
        )
    }
}
