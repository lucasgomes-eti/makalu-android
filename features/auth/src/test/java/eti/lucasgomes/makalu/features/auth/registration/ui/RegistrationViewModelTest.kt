package eti.lucasgomes.makalu.features.auth.registration.ui

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import eti.lucasgomes.makalu.components.CameraCaptureManager
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.auth.AuthClient
import eti.lucasgomes.makalu.features.auth.R
import eti.lucasgomes.makalu.features.auth.login.model.LoginRequest
import eti.lucasgomes.makalu.features.auth.makaluError
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterRequest
import eti.lucasgomes.makalu.features.auth.registration.model.RegisterResponse
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.shared.MAX_PASSWORD_LENGTH
import eti.lucasgomes.makalu.shared.MIN_PASSWORD_LENGTH
import eti.lucasgomes.makalu.shared.PROFILE_PIC_TEMP_FILE_PREFIX
import eti.lucasgomes.makalu.shared.model.ImageUploadResponse
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.OSNavigation
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.network.TokenPairResponse
import eti.lucasgomes.makalu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navigator: Navigator = mockk(relaxed = true)
    private val cameraCaptureManager: CameraCaptureManager = mockk()
    private val osNavigation: OSNavigation = mockk(relaxed = true)
    private val authClient: AuthClient = mockk()
    private val viewModel =
        RegistrationViewModel(navigator, cameraCaptureManager, osNavigation, authClient)

    private val imageUri = Uri.parse("content://media/images/pic.jpg")
    private val homeNavOptions = NavOptions(
        popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true)
    )
    private val registerResponse = RegisterResponse(1, NAME, EMAIL, PHONE)
    private val tokenPair = TokenPairResponse("access", "refresh")

    private val state get() = viewModel.uiState.value

    private fun fillForm(
        name: String = NAME,
        email: String = EMAIL,
        phone: String = PHONE,
        password: String = PASSWORD,
        confirmation: String = password
    ) {
        viewModel.onAction(RegistrationAction.NameChanged(name))
        viewModel.onAction(RegistrationAction.EmailChanged(email))
        viewModel.onAction(RegistrationAction.PhoneNumberChanged(phone))
        viewModel.onAction(RegistrationAction.PasswordChanged(password))
        viewModel.onAction(RegistrationAction.PasswordConfirmationChanged(confirmation))
    }

    private fun givenRegisterAndLoginSucceed() {
        coEvery { authClient.register(any()) } returns Resource.Success(registerResponse)
        coEvery { authClient.login(any()) } returns Resource.Success(tokenPair)
    }

    private fun createAccountWithMismatchedPasswords() {
        fillForm(confirmation = "other1234")
        viewModel.onAction(RegistrationAction.CreateAccountClicked)
    }

    @Test
    fun `profile image clicked shows image picker`() {
        viewModel.onAction(RegistrationAction.ProfileImageClicked)

        assertTrue(state.isImagePickerVisible)
    }

    @Test
    fun `image picker dismissed hides image picker`() {
        viewModel.onAction(RegistrationAction.ProfileImageClicked)

        viewModel.onAction(RegistrationAction.ImagePickerDismissed)

        assertFalse(state.isImagePickerVisible)
    }

    @Test
    fun `gallery image obtained hides picker and opens image preview`() {
        viewModel.onAction(RegistrationAction.ProfileImageClicked)

        viewModel.onAction(RegistrationAction.GalleryImageObtained(imageUri))

        assertFalse(state.isImagePickerVisible)
        coVerify(exactly = 1) {
            navigator.navigate(Destination.Screen.ImagePreview(imageUri.toString()))
        }
    }

    @Test
    fun `gallery image obtained with null uri does nothing`() {
        viewModel.onAction(RegistrationAction.ProfileImageClicked)
        val before = state

        viewModel.onAction(RegistrationAction.GalleryImageObtained(null))

        assertEquals(before, state)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `camera image taken hides picker and opens preview of captured uri`() {
        every { cameraCaptureManager.imageUri } returns imageUri
        viewModel.onAction(RegistrationAction.ProfileImageClicked)

        viewModel.onAction(RegistrationAction.CameraImageTaken(isImageSaved = true))

        assertFalse(state.isImagePickerVisible)
        coVerify(exactly = 1) {
            navigator.navigate(Destination.Screen.ImagePreview(imageUri.toString()))
        }
    }

    @Test
    fun `camera image not saved does nothing`() {
        viewModel.onAction(RegistrationAction.ProfileImageClicked)

        viewModel.onAction(RegistrationAction.CameraImageTaken(isImageSaved = false))

        assertTrue(state.isImagePickerVisible)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
    }

    @Test
    fun `cropped image received sets profile image`() {
        viewModel.onAction(RegistrationAction.CroppedImageReceived(imageUri))

        assertEquals(imageUri, state.profileImage)
        assertTrue(state.isProfileImageLoaded)
    }

    @Test
    fun `permission granted starts camera capture with profile prefix and forwards uri`() {
        coEvery { cameraCaptureManager.capture(any(), any()) } answers {
            secondArg<(Uri) -> Unit>().invoke(imageUri)
        }
        val launched = mutableListOf<Uri>()

        viewModel.onAction(
            RegistrationAction.PermissionLauncherResultReceived(isGranted = true) { launched += it }
        )

        coVerify(exactly = 1) { cameraCaptureManager.capture(PROFILE_PIC_TEMP_FILE_PREFIX, any()) }
        assertEquals(listOf(imageUri), launched)
        assertFalse(state.isPermissionDeniedDialogVisible)
    }

    @Test
    fun `permission denied shows permission denied dialog`() {
        viewModel.onAction(
            RegistrationAction.PermissionLauncherResultReceived(isGranted = false) {
            }
        )

        assertTrue(state.isPermissionDeniedDialogVisible)
        coVerify(exactly = 0) { cameraCaptureManager.capture(any(), any()) }
    }

    @Test
    fun `go to system settings hides dialog and opens app settings`() {
        viewModel.onAction(
            RegistrationAction.PermissionLauncherResultReceived(isGranted = false) {
            }
        )

        viewModel.onAction(RegistrationAction.GoToSystemSettingsClicked)

        assertFalse(state.isPermissionDeniedDialogVisible)
        verify(exactly = 1) { osNavigation.openApplicationSettings() }
    }

    @Test
    fun `permission denied dialog dismissed hides dialog`() {
        viewModel.onAction(
            RegistrationAction.PermissionLauncherResultReceived(isGranted = false) {
            }
        )

        viewModel.onAction(RegistrationAction.PermissionDeniedDialogDismissed)

        assertFalse(state.isPermissionDeniedDialogVisible)
        verify(exactly = 0) { osNavigation.openApplicationSettings() }
    }

    @Test
    fun `name changed updates text and clears name error`() {
        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        viewModel.onAction(RegistrationAction.NameChanged("Ana"))

        assertEquals("Ana", state.name.text)
        assertEquals(UiText.Empty, state.name.error)
    }

    @Test
    fun `name longer than 120 characters is ignored`() {
        viewModel.onAction(RegistrationAction.NameChanged("Ana"))

        viewModel.onAction(RegistrationAction.NameChanged("a".repeat(121)))
        assertEquals("Ana", state.name.text)

        viewModel.onAction(RegistrationAction.NameChanged("a".repeat(120)))
        assertEquals("a".repeat(120), state.name.text)
    }

    @Test
    fun `email changed updates text and clears email error`() {
        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        viewModel.onAction(RegistrationAction.EmailChanged(EMAIL))

        assertEquals(EMAIL, state.email.text)
        assertEquals(UiText.Empty, state.email.error)
    }

    @Test
    fun `email longer than 120 characters is ignored`() {
        viewModel.onAction(RegistrationAction.EmailChanged(EMAIL))

        viewModel.onAction(RegistrationAction.EmailChanged("e".repeat(121)))
        assertEquals(EMAIL, state.email.text)

        viewModel.onAction(RegistrationAction.EmailChanged("e".repeat(120)))
        assertEquals("e".repeat(120), state.email.text)
    }

    @Test
    fun `phone number changed updates text and clears phone error`() {
        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        viewModel.onAction(RegistrationAction.PhoneNumberChanged(PHONE))

        assertEquals(PHONE, state.phoneNumber.text)
        assertEquals(UiText.Empty, state.phoneNumber.error)
    }

    @Test
    fun `phone number longer than 15 characters is ignored`() {
        viewModel.onAction(RegistrationAction.PhoneNumberChanged(PHONE))

        viewModel.onAction(RegistrationAction.PhoneNumberChanged("1".repeat(16)))
        assertEquals(PHONE, state.phoneNumber.text)

        viewModel.onAction(RegistrationAction.PhoneNumberChanged("1".repeat(15)))
        assertEquals("1".repeat(15), state.phoneNumber.text)
    }

    @Test
    fun `password changed updates text and clears password and confirmation errors`() {
        createAccountWithMismatchedPasswords()

        viewModel.onAction(RegistrationAction.PasswordChanged("other1234"))

        assertEquals("other1234", state.password.text)
        assertEquals(UiText.Empty, state.password.error)
        assertEquals(UiText.Empty, state.passwordConfirmation.error)
    }

    @Test
    fun `password longer than max length is ignored`() {
        viewModel.onAction(RegistrationAction.PasswordChanged(PASSWORD))

        viewModel.onAction(
            RegistrationAction.PasswordChanged("a1".padEnd(MAX_PASSWORD_LENGTH + 1, 'x'))
        )
        assertEquals(PASSWORD, state.password.text)

        viewModel.onAction(
            RegistrationAction.PasswordChanged("a1".padEnd(MAX_PASSWORD_LENGTH, 'x'))
        )
        assertEquals("a1".padEnd(MAX_PASSWORD_LENGTH, 'x'), state.password.text)
    }

    @Test
    fun `password confirmation changed updates text and clears both password errors`() {
        createAccountWithMismatchedPasswords()

        viewModel.onAction(RegistrationAction.PasswordConfirmationChanged(PASSWORD))

        assertEquals(PASSWORD, state.passwordConfirmation.text)
        assertEquals(UiText.Empty, state.passwordConfirmation.error)
        assertEquals(UiText.Empty, state.password.error)
    }

    @Test
    fun `password confirmation longer than max length is ignored`() {
        viewModel.onAction(RegistrationAction.PasswordConfirmationChanged(PASSWORD))

        viewModel.onAction(
            RegistrationAction.PasswordConfirmationChanged(
                "a1".padEnd(MAX_PASSWORD_LENGTH + 1, 'x')
            )
        )
        assertEquals(PASSWORD, state.passwordConfirmation.text)

        viewModel.onAction(
            RegistrationAction.PasswordConfirmationChanged("a1".padEnd(MAX_PASSWORD_LENGTH, 'x'))
        )
        assertEquals("a1".padEnd(MAX_PASSWORD_LENGTH, 'x'), state.passwordConfirmation.text)
    }

    @Test
    fun `show password clicked toggles visibility`() {
        viewModel.onAction(RegistrationAction.ShowPasswordClicked)
        assertTrue(state.isPasswordVisible)

        viewModel.onAction(RegistrationAction.ShowPasswordClicked)
        assertFalse(state.isPasswordVisible)
    }

    @Test
    fun `dismiss error clears general error`() = runTest {
        coEvery { authClient.register(any()) } returns Resource.Error(makaluError())
        fillForm()
        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        viewModel.onAction(RegistrationAction.DismissError)

        assertEquals(UiText.Empty, state.generalError)
    }

    @Test
    fun `create account with empty form reports every required error`() {
        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertEquals(UiText.StringResource(R.string.name_is_required), state.name.error)
        assertEquals(UiText.StringResource(R.string.email_is_required), state.email.error)
        assertEquals(
            UiText.StringResource(R.string.phone_number_is_required),
            state.phoneNumber.error
        )
        assertEquals(UiText.StringResource(R.string.password_is_required), state.password.error)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authClient.register(any()) }
    }

    @Test
    fun `create account with malformed email sets invalid format error`() {
        fillForm(email = "not-an-email")

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertEquals(
            UiText.StringResource(R.string.email_must_have_valid_format),
            state.email.error
        )
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authClient.register(any()) }
    }

    @Test
    fun `create account with short password sets minimum length error`() {
        fillForm(password = "abc123")

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertEquals(
            UiText.StringResource(
                R.string.password_must_have_at_least_characters,
                listOf(MIN_PASSWORD_LENGTH)
            ),
            state.password.error
        )
        coVerify(exactly = 0) { authClient.register(any()) }
    }

    @Test
    fun `create account with password without digits sets letters and numbers error`() {
        fillForm(password = "abcdefgh")

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertEquals(
            UiText.StringResource(R.string.password_must_have_letters_and_numbers),
            state.password.error
        )
        coVerify(exactly = 0) { authClient.register(any()) }
    }

    @Test
    fun `create account with mismatched confirmation flags both password fields`() {
        createAccountWithMismatchedPasswords()

        val error = UiText.StringResource(R.string.passwords_do_not_match)
        assertEquals(error, state.password.error)
        assertEquals(error, state.passwordConfirmation.error)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authClient.register(any()) }
    }

    @Test
    fun `create account with valid form registers with the typed data`() = runTest {
        givenRegisterAndLoginSucceed()
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        coVerify(exactly = 1) { authClient.register(RegisterRequest(NAME, EMAIL, PHONE, PASSWORD)) }
    }

    @Test
    fun `create account shows loading while registering`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { authClient.register(any()) } coAnswers
            {
                gate.await()
                Resource.Error(makaluError())
            }
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)
        assertTrue(state.isLoading)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(state.isLoading)
    }

    @Test
    fun `create account clicked while loading is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { authClient.register(any()) } coAnswers
            {
                gate.await()
                Resource.Error(makaluError())
            }
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)
        viewModel.onAction(RegistrationAction.CreateAccountClicked)
        gate.complete(Unit)
        advanceUntilIdle()

        coVerify(exactly = 1) { authClient.register(any()) }
    }

    @Test
    fun `register failure assigns field errors and shows formatted general error`() = runTest {
        val error = makaluError(
            httpCode = 409,
            message = "Conflict",
            internalCode = "MK-409",
            fieldErrors = listOf(MakaluError.FieldError("email", "Email already in use"))
        )
        coEvery { authClient.register(any()) } returns Resource.Error(error)
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertEquals(UiText.PlainText("Email already in use"), state.email.error)
        assertEquals(UiText.Empty, state.name.error)
        assertEquals(UiText.Empty, state.password.error)
        assertEquals(UiText.PlainText("MK-409 - Conflict"), state.generalError)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authClient.login(any()) }
    }

    @Test
    fun `register success logs in with the same credentials`() = runTest {
        givenRegisterAndLoginSucceed()
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        coVerify(exactly = 1) { authClient.login(LoginRequest(EMAIL, PASSWORD)) }
    }

    @Test
    fun `login failure after register shows error and navigates up after five seconds`() = runTest {
        coEvery { authClient.register(any()) } returns Resource.Success(registerResponse)
        coEvery { authClient.login(any()) } returns Resource.Error(makaluError())
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertEquals(
            UiText.StringResource(R.string.account_created_but_login_failed),
            state.generalError
        )
        assertFalse(state.isLoading)
        advanceTimeBy(5.seconds - 1.milliseconds)
        coVerify(exactly = 0) { navigator.navigateUp() }
        advanceTimeBy(1.milliseconds)
        runCurrent()
        coVerify(exactly = 1) { navigator.navigateUp() }
    }

    @Test
    fun `login success without profile image navigates home`() = runTest {
        givenRegisterAndLoginSucceed()
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertFalse(state.isLoading)
        assertEquals(UiText.Empty, state.generalError)
        coVerify(exactly = 1) { navigator.navigate(Destination.Graph.Root, homeNavOptions) }
        coVerify(exactly = 0) { authClient.uploadImage(any()) }
    }

    @Test
    fun `login success with profile image uploads it`() = runTest {
        givenRegisterAndLoginSucceed()
        coEvery { authClient.uploadImage(any()) } returns Resource.Success(ImageUploadResponse(9))
        viewModel.onAction(RegistrationAction.CroppedImageReceived(imageUri))
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        coVerify(exactly = 1) { authClient.uploadImage(imageUri) }
    }

    @Test
    fun `image upload shows upload loading while in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        givenRegisterAndLoginSucceed()
        coEvery { authClient.uploadImage(any()) } coAnswers
            {
                gate.await()
                Resource.Success(ImageUploadResponse(9))
            }
        viewModel.onAction(RegistrationAction.CroppedImageReceived(imageUri))
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)
        assertTrue(state.imageUploadLoading)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(state.imageUploadLoading)
    }

    @Test
    fun `image upload success stops upload loading and navigates home`() = runTest {
        givenRegisterAndLoginSucceed()
        coEvery { authClient.uploadImage(any()) } returns Resource.Success(ImageUploadResponse(9))
        viewModel.onAction(RegistrationAction.CroppedImageReceived(imageUri))
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertFalse(state.imageUploadLoading)
        assertFalse(state.isLoading)
        coVerify(exactly = 1) { navigator.navigate(Destination.Graph.Root, homeNavOptions) }
    }

    @Test
    fun `image upload failure shows error then navigates home after five seconds`() = runTest {
        givenRegisterAndLoginSucceed()
        coEvery { authClient.uploadImage(any()) } returns
            Resource.Error(makaluError(500, "Upload failed", "MK-500"))
        viewModel.onAction(RegistrationAction.CroppedImageReceived(imageUri))
        fillForm()

        viewModel.onAction(RegistrationAction.CreateAccountClicked)

        assertEquals(
            UiText.StringResource(
                R.string.profile_image_upload_failed,
                listOf("MK-500 - Upload failed")
            ),
            state.generalError
        )
        assertFalse(state.imageUploadLoading)
        advanceTimeBy(5.seconds - 1.milliseconds)
        coVerify(exactly = 0) { navigator.navigate(any(), any()) }
        advanceTimeBy(1.milliseconds)
        runCurrent()
        coVerify(exactly = 1) { navigator.navigate(Destination.Graph.Root, homeNavOptions) }
        assertEquals(UiText.Empty, state.generalError)
        assertFalse(state.isLoading)
    }

    private companion object {
        const val NAME = "Ana Silva"
        const val EMAIL = "ana@makalu.com"
        const val PHONE = "11999999999"
        const val PASSWORD = "secret123"
    }
}
