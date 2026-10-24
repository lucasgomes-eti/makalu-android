package eti.lucasgomes.makalu.features.profile.ui

import MakaluConfig
import eti.lucasgomes.makalu.components.dsl.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUiStateTest {

    @Test
    fun `image url is null and image is not loaded without image id`() {
        val state = ProfileUiState()

        assertNull(state.imageUrl)
        assertFalse(state.isProfileImageLoaded)
    }

    @Test
    fun `image url is built from base url and image id`() {
        val state = ProfileUiState(imageId = 42)

        assertEquals("${MakaluConfig.BASE_URL}images/42", state.imageUrl)
        assertTrue(state.isProfileImageLoaded)
    }

    @Test
    fun `default state is idle with empty fields and no dialogs`() {
        val state = ProfileUiState()

        assertFalse(state.isLoading)
        assertFalse(state.isLogoutDialogVisible)
        assertFalse(state.isImagePickerVisible)
        assertFalse(state.isPermissionDeniedDialogVisible)
        assertFalse(state.imageUploadLoading)
        assertEquals(UiText.Empty, state.generalError)
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.phoneNumber)
    }
}
