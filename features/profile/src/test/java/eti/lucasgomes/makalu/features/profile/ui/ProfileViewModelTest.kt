package eti.lucasgomes.makalu.features.profile.ui

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.components.CameraCaptureManager
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.profile.ProfileClient
import eti.lucasgomes.makalu.features.profile.makaluError
import eti.lucasgomes.makalu.features.profile.model.ProfileAction
import eti.lucasgomes.makalu.features.profile.profileResponse
import eti.lucasgomes.makalu.shared.PROFILE_PIC_TEMP_FILE_PREFIX
import eti.lucasgomes.makalu.shared.model.ImageUploadResponse
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.OSNavigation
import eti.lucasgomes.makalu.shared.network.LogoutUseCase
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)
    private val profileClient: ProfileClient = mockk()
    private val navigator: Navigator = mockk(relaxed = true)
    private val cameraCaptureManager: CameraCaptureManager = mockk()
    private val osNavigation: OSNavigation = mockk(relaxed = true)

    @Before
    fun setUp() {
        coEvery { profileClient.getSelfProfile() } returns Resource.Success(profileResponse())
    }

    private fun createViewModel() = ProfileViewModel(
        logoutUseCase = logoutUseCase,
        profileClient = profileClient,
        navigator = navigator,
        cameraCaptureManager = cameraCaptureManager,
        osNavigation = osNavigation
    )

    @Test
    fun `init shows loading while the profile request is in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { profileClient.getSelfProfile() } coAnswers {
            gate.await()
            Resource.Success(profileResponse())
        }

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `init success fills name email phone number and image url`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Ana Silva", state.name)
        assertEquals("ana@makalu.com", state.email)
        assertEquals("11999999999", state.phoneNumber)
        assertEquals(ProfileUiState(imageId = 7).imageUrl, state.imageUrl)
        assertTrue(state.isProfileImageLoaded)
        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `init success without profile image leaves image url null`() = runTest {
        coEvery { profileClient.getSelfProfile() } returns Resource.Success(
            profileResponse(profileImageId = null)
        )

        val viewModel = createViewModel()

        assertNull(viewModel.uiState.value.imageUrl)
        assertFalse(viewModel.uiState.value.isProfileImageLoaded)
    }

    @Test
    fun `init failure sets general error from formatted message and stops loading`() = runTest {
        coEvery { profileClient.getSelfProfile() } returns Resource.Error(
            makaluError(404, "Not found", "MK-404")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UiText.PlainText("MK-404 - Not found"), state.generalError)
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.phoneNumber)
        assertNull(state.imageUrl)
    }

    @Test
    fun `init requests the profile exactly once`() = runTest {
        createViewModel()

        coVerify(exactly = 1) { profileClient.getSelfProfile() }
    }

    @Test
    fun `error dismiss clears the general error`() = runTest {
        coEvery { profileClient.getSelfProfile() } returns Resource.Error(makaluError())
        val viewModel = createViewModel()

        viewModel.onAction(ProfileAction.ErrorDismiss)

        assertEquals(UiText.Empty, viewModel.uiState.value.generalError)
    }

    @Test
    fun `logout clicked shows the logout dialog`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ProfileAction.LogoutClicked)

        assertTrue(viewModel.uiState.value.isLogoutDialogVisible)
    }

    @Test
    fun `logout dialog dismissed hides the logout dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.LogoutClicked)

        viewModel.onAction(ProfileAction.LogoutDialogDismissed)

        assertFalse(viewModel.uiState.value.isLogoutDialogVisible)
        coVerify(exactly = 0) { logoutUseCase.invoke() }
    }

    @Test
    fun `logout confirmed hides the dialog and runs the logout use case`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.LogoutClicked)

        viewModel.onAction(ProfileAction.LogoutConfirmed)

        assertFalse(viewModel.uiState.value.isLogoutDialogVisible)
        coVerify(exactly = 1) { logoutUseCase.invoke() }
    }

    @Test
    fun `profile image clicked shows the image picker`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ProfileAction.ProfileImageClicked)

        assertTrue(viewModel.uiState.value.isImagePickerVisible)
    }

    @Test
    fun `image picker dismissed hides the image picker`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.ProfileImageClicked)

        viewModel.onAction(ProfileAction.ImagePickerDismissed)

        assertFalse(viewModel.uiState.value.isImagePickerVisible)
    }

    @Test
    fun `gallery image obtained hides the picker and navigates to image preview`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.ProfileImageClicked)

        viewModel.onAction(ProfileAction.GalleryImageObtained(Uri.parse("content://media/1")))

        assertFalse(viewModel.uiState.value.isImagePickerVisible)
        coVerify(exactly = 1) {
            navigator.navigate(Destination.Screen.ImagePreview("content://media/1"))
        }
    }

    @Test
    fun `gallery image obtained with null uri does nothing`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.ProfileImageClicked)

        viewModel.onAction(ProfileAction.GalleryImageObtained(null))

        assertTrue(viewModel.uiState.value.isImagePickerVisible)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `camera image saved hides the picker and navigates to preview of the captured uri`() =
        runTest {
            val capturedUri = Uri.parse("content://provider/cache/profile_pic_1.jpg")
            every { cameraCaptureManager.imageUri } returns capturedUri
            val viewModel = createViewModel()
            viewModel.onAction(ProfileAction.ProfileImageClicked)

            viewModel.onAction(ProfileAction.CameraImageTaken(isImageSaved = true))

            assertFalse(viewModel.uiState.value.isImagePickerVisible)
            coVerify(exactly = 1) {
                navigator.navigate(
                    Destination.Screen.ImagePreview("content://provider/cache/profile_pic_1.jpg")
                )
            }
        }

    @Test
    fun `camera image not saved does nothing`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.ProfileImageClicked)

        viewModel.onAction(ProfileAction.CameraImageTaken(isImageSaved = false))

        assertTrue(viewModel.uiState.value.isImagePickerVisible)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `permission granted captures with the profile prefix and launches the camera with the captured uri`() =
        runTest {
            val capturedUri = Uri.parse("content://provider/cache/profile_pic_2.jpg")
            coEvery { cameraCaptureManager.capture(any(), any()) } coAnswers {
                secondArg<(Uri) -> Unit>().invoke(capturedUri)
            }
            val launchedUris = mutableListOf<Uri>()
            val viewModel = createViewModel()

            viewModel.onAction(
                ProfileAction.PermissionLauncherResultReceived(
                    isGranted = true,
                    launchCamera = { launchedUris += it }
                )
            )

            coVerify(exactly = 1) {
                cameraCaptureManager.capture(PROFILE_PIC_TEMP_FILE_PREFIX, any())
            }
            assertEquals(listOf(capturedUri), launchedUris)
            assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        }

    @Test
    fun `permission denied shows the permission denied dialog without capturing`() = runTest {
        val launchedUris = mutableListOf<Uri>()
        val viewModel = createViewModel()

        viewModel.onAction(
            ProfileAction.PermissionLauncherResultReceived(
                isGranted = false,
                launchCamera = { launchedUris += it }
            )
        )

        assertTrue(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        coVerify(exactly = 0) { cameraCaptureManager.capture(any(), any()) }
        assertTrue(launchedUris.isEmpty())
    }

    @Test
    fun `permission denied dialog dismissed hides the dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.PermissionLauncherResultReceived(false) {})

        viewModel.onAction(ProfileAction.PermissionDeniedDialogDismissed)

        assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        verify(exactly = 0) { osNavigation.openApplicationSettings() }
    }

    @Test
    fun `go to system settings hides the dialog and opens application settings`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ProfileAction.PermissionLauncherResultReceived(false) {})

        viewModel.onAction(ProfileAction.GoToSystemSettingsClicked)

        assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        verify(exactly = 1) { osNavigation.openApplicationSettings() }
    }

    @Test
    fun `cropped image received shows upload loading while uploading`() = runTest {
        val uri = Uri.parse("file:///cache/cropped.jpg")
        val gate = CompletableDeferred<Unit>()
        coEvery { profileClient.uploadImage(uri) } coAnswers {
            gate.await()
            Resource.Success(ImageUploadResponse(99))
        }
        val viewModel = createViewModel()

        viewModel.onAction(ProfileAction.CroppedImageReceived(uri))

        assertTrue(viewModel.uiState.value.imageUploadLoading)
        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.imageUploadLoading)
    }

    @Test
    fun `cropped image upload success stores new image and stops loading`() = runTest {
        val uri = Uri.parse("file:///cache/cropped.jpg")
        coEvery { profileClient.uploadImage(uri) } returns Resource.Success(ImageUploadResponse(99))
        val viewModel = createViewModel()

        viewModel.onAction(ProfileAction.CroppedImageReceived(uri))

        val state = viewModel.uiState.value
        coVerify(exactly = 1) { profileClient.uploadImage(uri) }
        assertFalse(state.imageUploadLoading)
        assertEquals(ProfileUiState(imageId = 99).imageUrl, state.imageUrl)
        assertEquals("Ana Silva", state.name)
        assertEquals("ana@makalu.com", state.email)
        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `cropped image upload failure sets general error and keeps previous image`() = runTest {
        val uri = Uri.parse("file:///cache/cropped.jpg")
        coEvery { profileClient.uploadImage(uri) } returns Resource.Error(
            makaluError(413, "Too large", "MK-413")
        )
        val viewModel = createViewModel()

        viewModel.onAction(ProfileAction.CroppedImageReceived(uri))

        val state = viewModel.uiState.value
        assertFalse(state.imageUploadLoading)
        assertEquals(UiText.PlainText("MK-413 - Too large"), state.generalError)
        assertEquals(ProfileUiState(imageId = 7).imageUrl, state.imageUrl)
    }
}
