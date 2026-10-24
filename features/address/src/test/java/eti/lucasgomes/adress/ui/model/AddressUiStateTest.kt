package eti.lucasgomes.adress.ui.model

import com.google.android.gms.maps.model.LatLng
import eti.lucasgomes.makalu.components.dsl.TextFieldState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.network.MakaluError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressUiStateTest {

    private fun AddressUiState.assign(vararg errors: MakaluError.FieldError) =
        assignFieldErrors(errors.toList()) as AddressUiState

    @Test
    fun `location is not provided by default`() {
        val state = AddressUiState()

        assertFalse(state.isLocationProvided)
    }

    @Test
    fun `location is provided when coordinates are set`() {
        val state = AddressUiState(location = LatLng(-23.5, -46.6))

        assertTrue(state.isLocationProvided)
    }

    @Test
    fun `marker is positioned at the location`() {
        val state = AddressUiState(location = LatLng(-23.5, -46.6))

        assertEquals(LatLng(-23.5, -46.6), state.marker.position)
    }

    @Test
    fun `assign field errors sets the street error from its field error`() {
        val state = AddressUiState()

        val result = state.assign(MakaluError.FieldError("street", "Too long"))

        assertEquals(UiText.PlainText("Too long"), result.street.error)
    }

    @Test
    fun `assign field errors clears errors of fields that are not reported`() {
        val state = AddressUiState(
            zipCode = TextFieldState(error = UiText.PlainText("Invalid zip")),
            street = TextFieldState(error = UiText.PlainText("Invalid street"))
        )

        val result = state.assign()

        assertEquals(UiText.Empty, result.zipCode.error)
        assertEquals(UiText.Empty, result.street.error)
    }

    @Test
    fun `assign field errors ignores fields the state does not map`() {
        val state = AddressUiState()

        val result = state.assign(MakaluError.FieldError("number", "Invalid number"))

        assertEquals(state, result)
    }

    @Test
    fun `assign field errors keeps field texts`() {
        val state = AddressUiState(
            zipCode = TextFieldState(text = "01310100"),
            street = TextFieldState(text = "Rua A")
        )

        val result = state.assign(MakaluError.FieldError("street", "Too long"))

        assertEquals("01310100", result.zipCode.text)
        assertEquals("Rua A", result.street.text)
    }
}
