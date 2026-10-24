package eti.lucasgomes.adress.ui

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import eti.lucasgomes.adress.AddressClient
import eti.lucasgomes.adress.FakeSettingsDataStore
import eti.lucasgomes.adress.addressResponse
import eti.lucasgomes.adress.failedTask
import eti.lucasgomes.adress.makaluError
import eti.lucasgomes.adress.model.AddressRequest
import eti.lucasgomes.adress.model.ReverseGeocodeResponse
import eti.lucasgomes.adress.pendingTask
import eti.lucasgomes.adress.successfulTask
import eti.lucasgomes.adress.ui.model.AddressAction
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.adress.R
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.Resource
import eti.lucasgomes.makalu.shared.settings.Settings
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
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class AddressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fusedClient: FusedLocationProviderClient = mockk()
    private val settingsClient: SettingsClient = mockk()
    private val app: Application = ApplicationProvider.getApplicationContext()
    private val addressClient: AddressClient = mockk()
    private val navigator: Navigator = mockk(relaxed = true)
    private var dataStore = FakeSettingsDataStore()

    @Before
    fun setUp() {
        coEvery { addressClient.getSelfAddress() } returns Resource.Error(makaluError(404, "Not found", "MK-404"))
    }

    private fun createViewModel() = AddressViewModel(
        fusedClient = fusedClient,
        settingsClient = settingsClient,
        app = app,
        addressClient = addressClient,
        navigator = navigator,
        dataStore = dataStore
    )

    private fun location(latitude: Double, longitude: Double) = Location("test").apply {
        this.latitude = latitude
        this.longitude = longitude
    }

    private fun stubLocationSettings(task: Task<LocationSettingsResponse>) {
        every { settingsClient.checkLocationSettings(any()) } returns task
    }

    private fun stubCurrentLocation(task: Task<Location>) {
        every { fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null) } returns task
    }

    private fun stubLocationFlow(
        latitude: Double = -23.5,
        longitude: Double = -46.6,
        geocode: ReverseGeocodeResponse = ReverseGeocodeResponse()
    ) {
        stubLocationSettings(successfulTask(mockk()))
        stubCurrentLocation(successfulTask(location(latitude, longitude)))
        coEvery { addressClient.reverseGeocode(latitude, longitude) } returns geocode
    }

    private fun resolvableException(): Pair<ResolvableApiException, PendingIntent> {
        val pendingIntent = PendingIntent.getActivity(app, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
        val exception = ResolvableApiException(Status(CommonStatusCodes.RESOLUTION_REQUIRED, null, pendingIntent))
        return exception to pendingIntent
    }

    private fun AddressViewModel.fillForm(
        zipCode: String = "01310100",
        street: String = "Rua A",
        number: String = "42",
        complement: String = "Apt 1"
    ) {
        onAction(AddressAction.ZipCodeChanged(zipCode))
        onAction(AddressAction.StreetChanged(street))
        onAction(AddressAction.NumberChanged(number))
        onAction(AddressAction.ComplementChanged(complement))
    }

    private fun AddressViewModel.obtainLocation() {
        onAction(AddressAction.GetCurrentLocationClicked(true))
    }

    @Test
    fun `init shows loading and location loading while the saved address is fetched`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { addressClient.getSelfAddress() } coAnswers {
            gate.await()
            Resource.Success(addressResponse())
        }

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isLocationLoading)
        gate.complete(Unit)
    }

    @Test
    fun `init success fills the form and location from the saved address`() = runTest {
        coEvery { addressClient.getSelfAddress() } returns Resource.Success(addressResponse())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLocationLoading)
        assertEquals("01310100", state.zipCode.text)
        assertEquals("Av. Paulista", state.street.text)
        assertEquals("1000", state.number.text)
        assertEquals("Apt 12", state.complement.text)
        assertEquals(LatLng(-23.56, -46.65), state.location)
    }

    @Test
    fun `init success maps null number and complement to empty text`() = runTest {
        coEvery { addressClient.getSelfAddress() } returns
            Resource.Success(addressResponse(number = null, complement = null))

        val viewModel = createViewModel()

        assertEquals("", viewModel.uiState.value.number.text)
        assertEquals("", viewModel.uiState.value.complement.text)
    }

    @Test
    fun `init not found stops loading without a general error`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLocationLoading)
        assertEquals(UiText.Empty, state.generalError)
        assertEquals("", state.zipCode.text)
        assertEquals("", state.street.text)
        assertEquals(LatLng(0.0, 0.0), state.location)
    }

    @Test
    fun `init failure other than not found sets the formatted general error`() = runTest {
        coEvery { addressClient.getSelfAddress() } returns Resource.Error(makaluError(500, "Boom", "MK-500"))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLocationLoading)
        assertEquals(UiText.PlainText("MK-500 - Boom"), state.generalError)
    }

    @Test
    fun `init fetches the saved address exactly once`() = runTest {
        createViewModel()

        coVerify(exactly = 1) { addressClient.getSelfAddress() }
    }

    @Test
    fun `zip code change updates text and clears its error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.SaveAddressClicked)

        viewModel.onAction(AddressAction.ZipCodeChanged("01310"))

        assertEquals("01310", viewModel.uiState.value.zipCode.text)
        assertEquals(UiText.Empty, viewModel.uiState.value.zipCode.error)
    }

    @Test
    fun `zip code change with eight characters is accepted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.ZipCodeChanged("01310100"))

        assertEquals("01310100", viewModel.uiState.value.zipCode.text)
    }

    @Test
    fun `zip code change longer than eight characters is ignored`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.ZipCodeChanged("01310100"))

        viewModel.onAction(AddressAction.ZipCodeChanged("013101001"))

        assertEquals("01310100", viewModel.uiState.value.zipCode.text)
    }

    @Test
    fun `street change updates text and clears its error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.SaveAddressClicked)

        viewModel.onAction(AddressAction.StreetChanged("Rua A"))

        assertEquals("Rua A", viewModel.uiState.value.street.text)
        assertEquals(UiText.Empty, viewModel.uiState.value.street.error)
    }

    @Test
    fun `number change updates text`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.NumberChanged("42"))

        assertEquals("42", viewModel.uiState.value.number.text)
    }

    @Test
    fun `complement change updates text`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.ComplementChanged("Apt 1"))

        assertEquals("Apt 1", viewModel.uiState.value.complement.text)
    }

    @Test
    fun `save with blank zip code sets zip code required error and does not call the client`() = runTest {
        stubLocationFlow()
        val viewModel = createViewModel()
        viewModel.obtainLocation()
        viewModel.onAction(AddressAction.StreetChanged("Rua A"))

        viewModel.onAction(AddressAction.SaveAddressClicked)

        val state = viewModel.uiState.value
        assertEquals(UiText.StringResource(R.string.zip_code_is_required), state.zipCode.error)
        assertEquals(UiText.Empty, state.street.error)
        assertEquals(UiText.Empty, state.locationError)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { addressClient.saveAddress(any()) }
        coVerify(exactly = 0) { addressClient.updateAddress(any(), any()) }
    }

    @Test
    fun `save with blank street sets street required error`() = runTest {
        stubLocationFlow()
        val viewModel = createViewModel()
        viewModel.obtainLocation()
        viewModel.onAction(AddressAction.ZipCodeChanged("01310100"))

        viewModel.onAction(AddressAction.SaveAddressClicked)

        val state = viewModel.uiState.value
        assertEquals(UiText.StringResource(R.string.street_name_is_required), state.street.error)
        assertEquals(UiText.Empty, state.zipCode.error)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { addressClient.saveAddress(any()) }
    }

    @Test
    fun `save without location sets location required error`() = runTest {
        val viewModel = createViewModel()
        viewModel.fillForm()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        val state = viewModel.uiState.value
        assertEquals(UiText.StringResource(R.string.location_is_required), state.locationError)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { addressClient.saveAddress(any()) }
    }

    @Test
    fun `save with an empty form reports every validation error at once`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        val state = viewModel.uiState.value
        assertEquals(UiText.StringResource(R.string.zip_code_is_required), state.zipCode.error)
        assertEquals(UiText.StringResource(R.string.street_name_is_required), state.street.error)
        assertEquals(UiText.StringResource(R.string.location_is_required), state.locationError)
    }

    @Test
    fun `save while loading is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { addressClient.getSelfAddress() } coAnswers {
            gate.await()
            Resource.Success(addressResponse())
        }
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        assertTrue(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { addressClient.saveAddress(any()) }
        coVerify(exactly = 0) { addressClient.updateAddress(any(), any()) }
        gate.complete(Unit)
    }

    @Test
    fun `save without saved address posts the request built from form and location`() = runTest {
        stubLocationFlow(latitude = -23.5, longitude = -46.6)
        coEvery { addressClient.saveAddress(any()) } returns Resource.Success(addressResponse())
        val viewModel = createViewModel()
        viewModel.obtainLocation()
        viewModel.fillForm()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        val expected = AddressRequest(
            zipCode = "01310100",
            street = "Rua A",
            number = "42",
            complement = "Apt 1",
            longitude = -46.6,
            latitude = -23.5
        )
        coVerify(exactly = 1) { addressClient.saveAddress(expected) }
        coVerify(exactly = 0) { addressClient.updateAddress(any(), any()) }
    }

    @Test
    fun `save shows loading while the request is in flight`() = runTest {
        stubLocationFlow()
        val gate = CompletableDeferred<Unit>()
        coEvery { addressClient.saveAddress(any()) } coAnswers {
            gate.await()
            Resource.Success(addressResponse())
        }
        val viewModel = createViewModel()
        viewModel.obtainLocation()
        viewModel.fillForm()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        assertTrue(viewModel.uiState.value.isLoading)
        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `save success stores the new address id and label and navigates up`() = runTest {
        dataStore = FakeSettingsDataStore(Settings(accessToken = "token"))
        stubLocationFlow()
        coEvery { addressClient.saveAddress(any()) } returns
            Resource.Success(addressResponse(id = 9, street = "Rua A", number = "42"))
        val viewModel = createViewModel()
        viewModel.obtainLocation()
        viewModel.fillForm()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        assertEquals(Settings(accessToken = "token", addressId = 9, addressName = "Rua A, 42"), dataStore.current)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { navigator.navigateUp() }
    }

    @Test
    fun `save failure sets the formatted general error and does not navigate`() = runTest {
        dataStore = FakeSettingsDataStore(Settings(accessToken = "token"))
        stubLocationFlow()
        coEvery { addressClient.saveAddress(any()) } returns Resource.Error(makaluError(400, "Invalid", "MK-1"))
        val viewModel = createViewModel()
        viewModel.obtainLocation()
        viewModel.fillForm()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        assertEquals(UiText.PlainText("MK-1 - Invalid"), viewModel.uiState.value.generalError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(Settings(accessToken = "token"), dataStore.current)
        coVerify(exactly = 0) { navigator.navigateUp() }
    }

    @Test
    fun `save with saved address puts the request to its id`() = runTest {
        coEvery { addressClient.getSelfAddress() } returns Resource.Success(addressResponse(id = 7))
        coEvery { addressClient.updateAddress(any(), any()) } returns Resource.Success(Unit)
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        val expected = AddressRequest(
            zipCode = "01310100",
            street = "Av. Paulista",
            number = "1000",
            complement = "Apt 12",
            longitude = -46.65,
            latitude = -23.56
        )
        coVerify(exactly = 1) { addressClient.updateAddress(expected, 7) }
        coVerify(exactly = 0) { addressClient.saveAddress(any()) }
    }

    @Test
    fun `update success refreshes the address label and navigates up`() = runTest {
        dataStore = FakeSettingsDataStore(Settings(addressId = 7, addressName = "Old"))
        coEvery { addressClient.getSelfAddress() } returns Resource.Success(addressResponse(id = 7))
        coEvery { addressClient.updateAddress(any(), any()) } returns Resource.Success(Unit)
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.StreetChanged("Rua B"))
        viewModel.onAction(AddressAction.NumberChanged("5"))

        viewModel.onAction(AddressAction.SaveAddressClicked)

        assertEquals(Settings(addressId = 7, addressName = "Rua B, 5"), dataStore.current)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { navigator.navigateUp() }
    }

    @Test
    fun `update failure sets the formatted general error and does not navigate`() = runTest {
        dataStore = FakeSettingsDataStore(Settings(addressId = 7, addressName = "Old"))
        coEvery { addressClient.getSelfAddress() } returns Resource.Success(addressResponse(id = 7))
        coEvery { addressClient.updateAddress(any(), any()) } returns
            Resource.Error(makaluError(409, "Conflict", "MK-2"))
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.SaveAddressClicked)

        assertEquals(UiText.PlainText("MK-2 - Conflict"), viewModel.uiState.value.generalError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(Settings(addressId = 7, addressName = "Old"), dataStore.current)
        coVerify(exactly = 0) { navigator.navigateUp() }
    }

    @Test
    fun `get current location without permission shows the permission denied dialog`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(false))

        assertTrue(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        verify(exactly = 0) { settingsClient.checkLocationSettings(any()) }
    }

    @Test
    fun `get current location with permission starts location loading and clears the location error`() = runTest {
        stubLocationSettings(pendingTask())
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.SaveAddressClicked)

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        assertTrue(viewModel.uiState.value.isLocationLoading)
        assertEquals(UiText.Empty, viewModel.uiState.value.locationError)
        verify(exactly = 1) { settingsClient.checkLocationSettings(any()) }
        verify(exactly = 0) { fusedClient.getCurrentLocation(any<Int>(), any()) }
    }

    @Test
    fun `satisfied location settings fetch the current location and fill the form from reverse geocode`() = runTest {
        stubLocationFlow(
            latitude = -23.5,
            longitude = -46.6,
            geocode = ReverseGeocodeResponse("01310100", "Rua A", "42", "Apt 1")
        )
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        val state = viewModel.uiState.value
        assertEquals(LatLng(-23.5, -46.6), state.location)
        assertEquals("01310100", state.zipCode.text)
        assertEquals("Rua A", state.street.text)
        assertEquals("42", state.number.text)
        assertEquals("Apt 1", state.complement.text)
        assertFalse(state.isLoading)
        assertFalse(state.isLocationLoading)
        verify(exactly = 1) { fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null) }
    }

    @Test
    fun `reverse geocode shows loading while in flight`() = runTest {
        stubLocationFlow()
        val gate = CompletableDeferred<Unit>()
        coEvery { addressClient.reverseGeocode(-23.5, -46.6) } coAnswers {
            gate.await()
            ReverseGeocodeResponse("01310100", "Rua A", "42", "Apt 1")
        }
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(LatLng(-23.5, -46.6), viewModel.uiState.value.location)
        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `resolvable location settings failure exposes the resolution without fetching location`() = runTest {
        val (exception, pendingIntent) = resolvableException()
        stubLocationSettings(failedTask(exception))
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        assertSame(pendingIntent, viewModel.uiState.value.locationSettingsResolution)
        assertTrue(viewModel.uiState.value.isLocationLoading)
        verify(exactly = 0) { fusedClient.getCurrentLocation(any<Int>(), any()) }
    }

    @Test
    fun `non resolvable location settings failure still fetches the current location`() = runTest {
        stubLocationSettings(failedTask(ApiException(Status.RESULT_INTERNAL_ERROR)))
        stubCurrentLocation(successfulTask(location(-23.5, -46.6)))
        coEvery { addressClient.reverseGeocode(-23.5, -46.6) } returns ReverseGeocodeResponse()
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        verify(exactly = 1) { fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null) }
        assertEquals(LatLng(-23.5, -46.6), viewModel.uiState.value.location)
        assertNull(viewModel.uiState.value.locationSettingsResolution)
    }

    @Test
    fun `null current location sets the location unavailable error`() = runTest {
        stubLocationSettings(successfulTask(mockk()))
        stubCurrentLocation(successfulTask(null))
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        val state = viewModel.uiState.value
        assertEquals(UiText.StringResource(R.string.location_unavailable), state.locationError)
        assertFalse(state.isLocationLoading)
        assertEquals(LatLng(0.0, 0.0), state.location)
        coVerify(exactly = 0) { addressClient.reverseGeocode(any(), any()) }
    }

    @Test
    fun `current location failure sets the error getting location`() = runTest {
        stubLocationSettings(successfulTask(mockk()))
        stubCurrentLocation(failedTask(RuntimeException("no fix")))
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        val state = viewModel.uiState.value
        assertEquals(UiText.StringResource(R.string.error_getting_location), state.locationError)
        assertFalse(state.isLocationLoading)
        coVerify(exactly = 0) { addressClient.reverseGeocode(any(), any()) }
    }

    @Test
    fun `location settings prompt shown clears the resolution`() = runTest {
        stubLocationSettings(failedTask(resolvableException().first))
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        viewModel.onAction(AddressAction.LocationSettingsPromptShown)

        assertNull(viewModel.uiState.value.locationSettingsResolution)
    }

    @Test
    fun `location settings enabled fetches the current location`() = runTest {
        stubLocationSettings(failedTask(resolvableException().first))
        stubCurrentLocation(successfulTask(location(-23.5, -46.6)))
        coEvery { addressClient.reverseGeocode(-23.5, -46.6) } returns ReverseGeocodeResponse()
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        viewModel.onAction(AddressAction.LocationSettingsResult(true))

        verify(exactly = 1) { fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null) }
        assertEquals(LatLng(-23.5, -46.6), viewModel.uiState.value.location)
        assertFalse(viewModel.uiState.value.isLocationLoading)
    }

    @Test
    fun `location settings declined sets services disabled error and stops location loading`() = runTest {
        stubLocationSettings(failedTask(resolvableException().first))
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.GetCurrentLocationClicked(true))

        viewModel.onAction(AddressAction.LocationSettingsResult(false))

        val state = viewModel.uiState.value
        assertFalse(state.isLocationLoading)
        assertEquals(UiText.StringResource(R.string.location_services_disabled), state.locationError)
        verify(exactly = 0) { fusedClient.getCurrentLocation(any<Int>(), any()) }
    }

    @Test
    fun `permission denied dismissed hides the dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.GetCurrentLocationClicked(false))

        viewModel.onAction(AddressAction.PermissionDeniedDismissed)

        assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
    }

    @Test
    fun `permission rationale requested shows the rationale dialog`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AddressAction.PermissionRationaleRequested)

        assertTrue(viewModel.uiState.value.isPermissionRationaleDialogVisible)
    }

    @Test
    fun `permission rationale dismissed hides the rationale dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.PermissionRationaleRequested)

        viewModel.onAction(AddressAction.PermissionRationaleDismissed)

        assertFalse(viewModel.uiState.value.isPermissionRationaleDialogVisible)
    }

    @Test
    fun `go to system settings hides the denied dialog and opens the app details settings`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(AddressAction.GetCurrentLocationClicked(false))

        viewModel.onAction(AddressAction.GoToSystemSettingsClicked)

        assertFalse(viewModel.uiState.value.isPermissionDeniedDialogVisible)
        val intent = shadowOf(app).nextStartedActivity
        assertEquals(ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals(Uri.fromParts("package", app.packageName, null), intent.data)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }
}
