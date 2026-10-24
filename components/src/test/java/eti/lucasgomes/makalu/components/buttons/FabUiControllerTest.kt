package eti.lucasgomes.makalu.components.buttons

import androidx.compose.ui.graphics.Color
import eti.lucasgomes.makalu.components.testPainter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FabUiControllerTest {

    private val controller = FabUiController()

    @Test
    fun `fab defaults to hidden enabled and collapsed without action`() {
        assertFalse(controller.isFabVisible.value)
        assertNull(controller.action.value)
        assertFalse(controller.isExtended.value)
        assertEquals("", controller.label.value)
        assertTrue(controller.isEnabled.value)
    }

    @Test
    fun `show makes the fab visible`() {
        controller.show()

        assertTrue(controller.isFabVisible.value)
    }

    @Test
    fun `hide hides the fab after show`() {
        controller.show()

        controller.hide()

        assertFalse(controller.isFabVisible.value)
    }

    @Test
    fun `setAction stores the action`() {
        var clicked = false
        val action = FabAction(testPainter(), "add") { clicked = true }

        controller.setAction(action)
        controller.action.value?.onClick?.invoke()

        assertSame(action, controller.action.value)
        assertTrue(clicked)
    }

    @Test
    fun `setAction replaces the previous action`() {
        val first = FabAction(testPainter(Color.Red), "first") {}
        val second = FabAction(testPainter(Color.Blue), "second") {}
        controller.setAction(first)

        controller.setAction(second)

        assertSame(second, controller.action.value)
    }

    @Test
    fun `setExtended updates the extended flag`() {
        controller.setExtended(true)
        val extended = controller.isExtended.value
        controller.setExtended(false)

        assertTrue(extended)
        assertFalse(controller.isExtended.value)
    }

    @Test
    fun `setLabel updates the label`() {
        controller.setLabel("Checkout")

        assertEquals("Checkout", controller.label.value)
    }

    @Test
    fun `setEnabled updates the enabled flag`() {
        controller.setEnabled(false)

        assertFalse(controller.isEnabled.value)
    }
}
