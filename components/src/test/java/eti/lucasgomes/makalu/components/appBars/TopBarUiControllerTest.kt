package eti.lucasgomes.makalu.components.appBars

import androidx.compose.ui.graphics.Color
import eti.lucasgomes.makalu.components.testPainter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopBarUiControllerTest {

    private val controller = TopBarUiController()

    private val actionA = TopBarAction(testPainter(Color.Red), "a") {}
    private val actionB = TopBarAction(testPainter(Color.Green), "b") {}
    private val actionC = TopBarAction(testPainter(Color.Blue), "c") {}

    @Test
    fun `top bar is hidden by default`() {
        assertFalse(controller.isTopBarVisible.value)
        assertEquals("", controller.title.value)
        assertTrue(controller.actions.isEmpty())
    }

    @Test
    fun `show makes the top bar visible`() {
        controller.show()

        assertTrue(controller.isTopBarVisible.value)
    }

    @Test
    fun `hide hides the top bar after show`() {
        controller.show()

        controller.hide()

        assertFalse(controller.isTopBarVisible.value)
    }

    @Test
    fun `setTitle updates the title`() {
        controller.setTitle("Orders")

        assertEquals("Orders", controller.title.value)
    }

    @Test
    fun `setNavigationActions sets the given actions in order`() {
        controller.setNavigationActions(listOf(actionA, actionB))

        assertEquals(listOf(actionA, actionB), controller.actions.toList())
    }

    @Test
    fun `setNavigationActions replaces the previous actions`() {
        controller.setNavigationActions(listOf(actionA, actionB))

        controller.setNavigationActions(listOf(actionC))

        assertEquals(listOf(actionC), controller.actions.toList())
    }

    @Test
    fun `setNavigationActions with an empty list clears the actions`() {
        controller.setNavigationActions(listOf(actionA))

        controller.setNavigationActions(emptyList())

        assertTrue(controller.actions.isEmpty())
    }
}
