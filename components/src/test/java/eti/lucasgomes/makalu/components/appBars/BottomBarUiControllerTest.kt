package eti.lucasgomes.makalu.components.appBars

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarUiControllerTest {

    private val controller = BottomBarUiController()

    @Test
    fun `bottom bar is visible by default`() {
        assertTrue(controller.isBottomBarVisible.value)
    }

    @Test
    fun `hide hides the bottom bar`() {
        controller.hide()

        assertFalse(controller.isBottomBarVisible.value)
    }

    @Test
    fun `show makes the bottom bar visible after hide`() {
        controller.hide()

        controller.show()

        assertTrue(controller.isBottomBarVisible.value)
    }
}
