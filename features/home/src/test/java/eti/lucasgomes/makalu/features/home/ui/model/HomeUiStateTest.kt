package eti.lucasgomes.makalu.features.home.ui.model

import eti.lucasgomes.makalu.components.dsl.UiText
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiStateTest {

    private fun category(id: Long, isSelected: Boolean) =
        CategoryUiState(id, UiText.PlainText("Category $id"), isSelected)

    @Test
    fun `selectedCategoriesIds is empty when there are no categories`() {
        assertEquals(emptyList<Long>(), HomeUiState().selectedCategoriesIds)
    }

    @Test
    fun `selectedCategoriesIds is empty when no category is selected`() {
        val state = HomeUiState(categories = listOf(category(1, false), category(2, false)))

        assertEquals(emptyList<Long>(), state.selectedCategoriesIds)
    }

    @Test
    fun `selectedCategoriesIds returns ids of selected categories in list order`() {
        val state = HomeUiState(
            categories = listOf(category(3, true), category(1, false), category(2, true))
        )

        assertEquals(listOf(3L, 2L), state.selectedCategoriesIds)
    }
}
